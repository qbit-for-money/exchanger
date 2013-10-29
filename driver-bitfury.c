/*
 * device-bitfury.c - device functions for Bitfury chip/board library
 *
 * Copyright (c) 2013 bitfury
 * Copyright (c) 2013 legkodymov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
*/

#include "miner.h"
#include <unistd.h>
#include <sha2.h>
#include "libbitfury.h"
#include "util.h"
#include "config.h"

#ifdef HAVE_CURSES
#include <curses.h>
#include <stdarg.h>
#endif

#define GOLDEN_BACKLOG 5

#ifdef HAVE_CURSES
extern WINDOW *mainwin, *statuswin, *per_dev_stat_win, *logwin;
#endif

struct device_drv bitfury_drv;

// Forward declarations
static void bitfury_disable(struct thr_info* thr);
static bool bitfury_prepare(struct thr_info *thr);
int calc_stat(time_t * stat_ts, time_t stat, struct timeval now);
double shares_to_ghashes(int shares, int seconds);
static void get_options(struct cgpu_info *cgpu);

static void bitfury_detect(void)
{
	int chip_n;
	int i;
	struct cgpu_info *bitfury_info;

	bitfury_info = calloc(1, sizeof(struct cgpu_info));
	bitfury_info->drv = &bitfury_drv;
	bitfury_info->threads = 1;

	applog(LOG_INFO, "INFO: bitfury_detect");
	chip_n = libbitfury_detectChips(bitfury_info->devices);
	if (!chip_n) {
		applog(LOG_WARNING, "No Bitfury chips detected!");
		return;
	} else {
		applog(LOG_WARNING, "BITFURY: %d chips detected!", chip_n);
	}

	bitfury_info->chip_n = chip_n;
	add_cgpu(bitfury_info);
}

static uint32_t bitfury_checkNonce(struct work *work, uint32_t nonce)
{
	applog(LOG_INFO, "INFO: bitfury_checkNonce");
}

static int bitfury_submitNonce(struct thr_info *thr, struct bitfury_device *device, struct timeval *now, struct work *owork, uint32_t nonce)
{
	int i;
	int is_dupe = 0;

	for(i=0; i<32; i++) {
		if(device->nonces[i] == nonce) {
		    is_dupe = 1;
		    break;
		}
	}

	if(!is_dupe) {
		submit_nonce(thr, owork, nonce);
		device->nonces[device->current_nonce++] = nonce;
		if(device->current_nonce > 32)
			device->current_nonce = 0;
		device->stat_ts[device->stat_counter++] = now->tv_sec;
		if (device->stat_counter == BITFURY_STAT_N)
			device->stat_counter = 0;
	}

	return(!is_dupe);
}

static int64_t bitfury_scanHash(struct thr_info *thr)
{
	static struct bitfury_device *devices, *dev; // TODO Move somewhere to appropriate place
	int chip_n;
	int chip;
	uint64_t hashes = 0;
	struct timeval now;
	unsigned char line[2048];
	int short_stat = 10;
	static time_t short_out_t;
	int long_stat = 600;
	static time_t long_out_t;
	int long_long_stat = 60 * 30;
	static time_t long_long_out_t;
	static first = 0; //TODO Move to detect()
	int i;
	int nonces_cnt;

	devices = thr->cgpu->devices;
	chip_n = thr->cgpu->chip_n;

	if (!first) {
		for (i = 0; i < chip_n; i++) {
			devices[i].osc6_bits = devices[i].osc6_bits_setpoint;
			devices[i].osc6_req = devices[i].osc6_bits_setpoint;
		}
		for (i = 0; i < chip_n; i++) {
			send_reinit(devices[i].slot, devices[i].fasync, devices[i].osc6_bits);
		}
	}
	first = 1;

	for (chip = 0; chip < chip_n; chip++) {
		dev = &devices[chip];
		dev->job_switched = 0;
		if(!dev->work) {
			dev->work = get_queued(thr->cgpu);
			if (dev->work == NULL) {
				return 0;
			}
			work_to_payload(&(dev->payload), dev->work);
		}
	}

	libbitfury_sendHashData(thr, devices, chip_n);

	cgtime(&now);
	chip = 0;
	for (;chip < chip_n; chip++) {
		nonces_cnt = 0;
		dev = &devices[chip];
		if (dev->job_switched) {
			int j;
			int *res = dev->results;
			struct work *work = dev->work;
			struct work *owork = dev->owork;
			struct work *o2work = dev->o2work;
			for (j = dev->results_n-1; j >= 0; j--) {
				if (owork) {
					nonces_cnt += bitfury_submitNonce(thr, dev, &now, owork, bswap_32(res[j]));
				}
				if (o2work) {
					// TEST
					//submit_nonce(thr, owork, bswap_32(res[j]));
				}
			}
			dev->results_n = 0;
			dev->job_switched = 0;
			if (dev->old_nonce && o2work)
				nonces_cnt += bitfury_submitNonce(thr, dev, &now, o2work, bswap_32(dev->old_nonce));

			if (dev->future_nonce)
				nonces_cnt += bitfury_submitNonce(thr, dev, &now, work, bswap_32(dev->future_nonce));

			if (o2work)
				work_completed(thr->cgpu, o2work);

			dev->o2work = dev->owork;
			dev->owork = dev->work;
			dev->work = NULL;
			hashes += 0xffffffffull * nonces_cnt;
			dev->matching_work += nonces_cnt;
		}
	}
#ifdef BITFURY_ENABLE_SHORT_STAT
	if (now.tv_sec - short_out_t > short_stat) {
		int shares_first = 0, shares_last = 0, shares_total = 0;
		char stat_lines[BITFURY_MAXBANKS][256] = {0};
		int len, k;
		double gh[BITFURY_MAXBANKS][BITFURY_BANKCHIPS] = {0};
		double ghsum = 0, gh1h = 0, gh2h = 0;
		unsigned strange_counter = 0;

		for (chip = 0; chip < chip_n; chip++) {
			dev = &devices[chip];
			int shares_found = calc_stat(dev->stat_ts, short_stat, now);
			double ghash;
			len = strlen(stat_lines[dev->slot]);
			ghash = shares_to_ghashes(shares_found, short_stat);
			gh[dev->slot][chip % BITFURY_BANKCHIPS] = ghash;
			snprintf(stat_lines[dev->slot] + len, 256 - len, "%.1f-%3.0f ", ghash, dev->mhz);

			if(short_out_t && ghash < 0.5) {
				//applog(LOG_WARNING, "Chip_id %d FREQ CHANGE", chip);
				send_freq(dev->slot, dev->fasync, dev->osc6_bits - 1);
				nmsleep(1);
				send_freq(dev->slot, dev->fasync, dev->osc6_bits);
			}
			shares_total += shares_found;
			shares_first += chip < BITFURY_BANKCHIPS/2 ? shares_found : 0;
			shares_last += chip >= BITFURY_BANKCHIPS/2 ? shares_found : 0;
			strange_counter += dev->hw_errors;
			//dev->strange_counter = 0;
		}
		sprintf(line, "vvvvwww SHORT stat %ds: wwwvvvv", short_stat);
		applog(LOG_WARNING, line);
		//sprintf(line, "stranges: %u", strange_counter);
		applog(LOG_WARNING, line);
		for(i = 0; i < BITFURY_MAXBANKS; i++)
			if(strlen(stat_lines[i])) {
				len = strlen(stat_lines[i]);
				ghsum = 0;
				gh1h = 0;
				gh2h = 0;
				for(k = 0; k < BITFURY_BANKCHIPS/2; k++) {
					gh1h += gh[i][k];
					gh2h += gh[i][k + BITFURY_BANKCHIPS/2];
					ghsum += gh[i][k] + gh[i][k + BITFURY_BANKCHIPS/2];
				}
				snprintf(stat_lines[i] + len, 256 - len, "- %2.1f + %2.1f = %2.1f slot %i ", gh1h, gh2h, ghsum, i);
				applog(LOG_WARNING, stat_lines[i]);
			}
		short_out_t = now.tv_sec;
	}
#endif
#ifdef BITFURY_ENABLE_LONG_STAT
	if (now.tv_sec - long_out_t > long_stat) {
		int shares_first = 0, shares_last = 0, shares_total = 0;
		char stat_lines[BITFURY_MAXBANKS][256] = {0};
		int len, k;
		double gh[BITFURY_MAXBANKS][BITFURY_BANKCHIPS] = {0};
		double ghsum = 0, gh1h = 0, gh2h = 0;

		for (chip = 0; chip < chip_n; chip++) {
			dev = &devices[chip];
			int shares_found = calc_stat(dev->stat_ts, long_stat, now);
			double ghash;
			len = strlen(stat_lines[dev->slot]);
			ghash = shares_to_ghashes(shares_found, long_stat);
			gh[dev->slot][chip % BITFURY_BANKCHIPS] = ghash;
			snprintf(stat_lines[dev->slot] + len, 256 - len, "%.1f-%3.0f ", ghash, dev->mhz);
			shares_total += shares_found;
			shares_first += chip < BITFURY_BANKCHIPS/2 ? shares_found : 0;
			shares_last += chip >= BITFURY_BANKCHIPS/2 ? shares_found : 0;
		}
		sprintf(line, "!!!_________ LONG stat %ds: ___________!!!", long_stat);
		applog(LOG_WARNING, line);
		for(i = 0; i < BITFURY_MAXBANKS; i++)
			if(strlen(stat_lines[i])) {
				len = strlen(stat_lines[i]);
				ghsum = 0;
				gh1h = 0;
				gh2h = 0;
				for(k = 0; k < BITFURY_BANKCHIPS/2; k++) {
					gh1h += gh[i][k];
					gh2h += gh[i][k + BITFURY_BANKCHIPS/2];
					ghsum += gh[i][k] + gh[i][k + BITFURY_BANKCHIPS/2];
				}
				snprintf(stat_lines[i] + len, 256 - len, "- %2.1f + %2.1f = %2.1f slot %i ", gh1h, gh2h, ghsum, i);
				applog(LOG_WARNING, stat_lines[i]);
			}
		long_out_t = now.tv_sec;
	}
#endif
#ifdef BITFURY_ENABLE_PER_DEV_STAT
	if (now.tv_sec - short_out_t > short_stat) {
		char stat_line[BITFURY_PER_DEV_STAT_LINE_LEN] = {0};
		int len = 0;
		double ghsum = 0;
		int n = 0;
		for (chip = 0; chip < chip_n; chip++) {
			dev = &devices[chip];
			if (dev->slot != BITFURY_PER_DEV_STAT_BANK_NUM) {
				continue;
			}
			int shares_found = calc_stat(dev->stat_ts, long_stat, now);
			double ghash = shares_to_ghashes(shares_found, long_stat);
			len = strlen(stat_line);
			if (n > 0) {
				snprintf(stat_line + len, BITFURY_PER_DEV_STAT_LINE_LEN - len, ",");
				len++;
			}
			snprintf(stat_line + len, BITFURY_PER_DEV_STAT_LINE_LEN - len, "%2.1f", ghash);
			if ((n > 0) && (n % BITFURY_PER_DEV_STAT_DEVS_GROUP_SIZE == 0)) {
				len = strlen(stat_line);
				snprintf(stat_line + len, BITFURY_PER_DEV_STAT_LINE_LEN - len, "|");
			}
			ghsum += ghash;
			n++;
		}
		mvwprintw(per_dev_stat_win, 0, 0, "%s", stat_line);
		mvwprintw(per_dev_stat_win, 1, 0, "CT: %.1fGhs", ghsum);
		long_out_t = now.tv_sec;
	}
#endif

	nmsleep(BITFURY_SCANHASH_DELAY);

	return hashes;
}

double shares_to_ghashes(int shares, int seconds) {
	return ( (double)shares * 4.294967296 ) / ( (double)seconds );

}

int calc_stat(time_t * stat_ts, time_t stat, struct timeval now) {
	int j;
	int shares_found = 0;
	for(j = 0; j < BITFURY_STAT_N; j++) {
		if (now.tv_sec - stat_ts[j] < stat) {
			shares_found++;
		}
	}
	return shares_found;
}

static void bitfury_statline_before(char *buf, struct cgpu_info *cgpu)
{
	applog(LOG_INFO, "INFO bitfury_statline_before");
}

static bool bitfury_prepare(struct thr_info *thr)
{
	struct timeval now;
	struct cgpu_info *cgpu = thr->cgpu;

	cgtime(&now);
	get_datestamp(cgpu->init, &now);

	get_options(cgpu);

	applog(LOG_INFO, "INFO bitfury_prepare");
	return true;
}

static void bitfury_shutdown(struct thr_info *thr)
{
	int chip_n;
	int i;

	chip_n = thr->cgpu->chip_n;

	applog(LOG_INFO, "INFO bitfury_shutdown");
	libbitfury_shutdownChips(thr->cgpu->devices, chip_n);
}

static void bitfury_disable(struct thr_info *thr)
{
	applog(LOG_INFO, "INFO bitfury_disable");
}

static int bitfury_findChip(struct bitfury_device *devices, int chip_n, int slot, int fs) {
	int n;
	for (n = 0; n < chip_n; n++) {
		if ( (devices[n].slot == slot) && (devices[n].fasync == fs) )
			return n;
	}
	return -1;
}

static void get_options(struct cgpu_info *cgpu)
{
	char buf[BUFSIZ+1];
	char *ptr, *comma, *colon, *colon2;
	size_t max = 0;
	int i, slot, fs, bits, chip, def_bits;

	for(i=0; i<cgpu->chip_n; i++)
		cgpu->devices[i].osc6_bits_setpoint = 54; // this is default value

	if (opt_bitfury_clockbits == NULL) {
		buf[0] = '\0';
		return;
	}

	ptr = opt_bitfury_clockbits;

	do {
		comma = strchr(ptr, ',');
		if (comma == NULL)
			max = strlen(ptr);
		else
			max = comma - ptr;
		if (max > BUFSIZ)
			max = BUFSIZ;
		strncpy(buf, ptr, max);
		buf[max] = '\0';

		if (*buf) {
			colon = strchr(buf, ':');
			if (colon) {
				*(colon++) = '\0';
				colon2 = strchr(colon, ':');
				if (colon2)
					*(colon2++) = '\0';
				if (*buf && *colon && *colon2) {
					slot = atoi(buf);
					fs = atoi(colon);
					bits = atoi(colon2);
					chip = bitfury_findChip(cgpu->devices, cgpu->chip_n, slot, fs);
					if(chip > 0 && chip < cgpu->chip_n && bits >= 48 && bits <= 56) {
						cgpu->devices[chip].osc6_bits_setpoint = bits;
						applog(LOG_INFO, "Set clockbits: slot=%d chip=%d bits=%d", slot, fs, bits);
					}
				}
			} else {
				def_bits = atoi(buf);
				if(def_bits >= 48 && def_bits <= 56) {
					for(i=0; i<cgpu->chip_n; i++)
						cgpu->devices[i].osc6_bits_setpoint = def_bits;
				}
			}
		}
		if(comma != NULL)
			ptr = ++comma;
	} while (comma != NULL);
}

static struct api_data *bitfury_api_stats(struct cgpu_info *cgpu)
{
	struct api_data *root = NULL;
	static struct bitfury_device *devices;
	struct timeval now;
	struct bitfury_info *info = cgpu->device_data;
	int shares_found, i;
	double ghash, ghash_sum = 0.0;
	char mcw[24];
	uint64_t total_hw = 0;

	devices = cgpu->devices;
	root = api_add_int(root, "chip_n", &(cgpu->chip_n),false);
	cgtime(&now);

	for (i = 0; i < cgpu->chip_n; i++) {
		sprintf(mcw, "clock_bits_%d_%d", devices[i].slot, devices[i].fasync);
		root = api_add_int(root, mcw, &(devices[i].osc6_bits), false);
	}
	for (i = 0; i < cgpu->chip_n; i++) {
		sprintf(mcw, "match_work_count_%d_%d", devices[i].slot, devices[i].fasync);
		root = api_add_uint(root, mcw, &(devices[i].matching_work), false);
	}
	for (i = 0; i < cgpu->chip_n; i++) {
		sprintf(mcw, "hw_errors_%d_%d", devices[i].slot, devices[i].fasync);
		root = api_add_uint(root, mcw, &(devices[i].hw_errors), false);
		total_hw += devices[i].hw_errors;
	}
//	for (i = 0; i < cgpu->chip_n; i++) {
//		sprintf(mcw, "mhz_%d_%d", devices[i].slot, devices[i].fasync);
//		root = api_add_double(root, mcw, &(devices[i].mhz), false);
//	}
	for (i = 0; i < cgpu->chip_n; i++) {
		shares_found = calc_stat(devices[i].stat_ts, BITFURY_API_STATS, now);
		ghash = shares_to_ghashes(shares_found, BITFURY_API_STATS);
		ghash_sum += ghash;
		sprintf(mcw, "ghash_%d_%d", devices[i].slot, devices[i].fasync);
		root = api_add_double(root, mcw, &(ghash), true);
	}
	api_add_uint64(root, "total_hw", &(total_hw), false);
	api_add_double(root, "total_gh", &(ghash_sum), true);
	ghash_sum /= cgpu->chip_n;
	api_add_double(root, "avg_gh_per_chip", &(ghash_sum), true);

	return root;
}

struct device_drv bitfury_drv = {
	.drv_id = DRIVER_BITFURY,
	.dname = "bitfury",
	.name = "BITFURY",
	.drv_detect = bitfury_detect,
	.get_statline_before = bitfury_statline_before,
	.thread_prepare = bitfury_prepare,
	.scanwork = bitfury_scanHash,
	.thread_shutdown = bitfury_shutdown,
	.hash_work = hash_queued_work,
	.get_api_stats = bitfury_api_stats,
};

