/**
 * libbitfury.c - library for Bitfury chip/board
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
**/

#include "config.h"

#include <stdio.h>
#include <unistd.h>
#include <string.h>

#include "miner.h"
#include "tm_i2c.h"
#include "libbitfury.h"

#include "spidevc.h"
#include "sha2.h"

#include "gpio.h"

#include "bank.h"

#include <time.h>

#ifdef HAVE_CURSES
#include <curses.h>
#include <stdarg.h>
#endif

#define BITFURY_REFRESH_DELAY 100
#define BITFURY_DETECT_TRIES 3000 / BITFURY_REFRESH_DELAY

#ifdef HAVE_CURSES
extern WINDOW *mainwin, *statuswin, *per_dev_stat_win, *logwin;
#endif

// 0 .... 31 bit
// 1000 0011 0101 0110 1001 1010 1100 0111

// 1100 0001 0110 1010 0101 1001 1110 0011
// C16A59E3

unsigned char enaconf[4] = { 0xc1, 0x6a, 0x59, 0xe3 };
unsigned char disconf[4] = { 0, 0, 0, 0 };

unsigned decnonce(unsigned in);

/* Configuration registers - control oscillators and such stuff. PROGRAMMED when magic number is matches, UNPROGRAMMED (default) otherwise */
void config_reg(int cfgreg, int ena)
{
	if (ena) spi_emit_data(0x7000+cfgreg*32, (void*)enaconf, 4);
	else     spi_emit_data(0x7000+cfgreg*32, (void*)disconf, 4);
}

#define FIRST_BASE 61
#define SECOND_BASE 4
char counters[16] = { 64, 64,
	SECOND_BASE, SECOND_BASE+4, SECOND_BASE+2, SECOND_BASE+2+16, SECOND_BASE, SECOND_BASE+1,
	(FIRST_BASE)%65,  (FIRST_BASE+1)%65,  (FIRST_BASE+3)%65, (FIRST_BASE+3+16)%65, (FIRST_BASE+4)%65, (FIRST_BASE+4+4)%65, (FIRST_BASE+3+3)%65, (FIRST_BASE+3+1+3)%65};

//char counters[16] = { 64, 64,
//	SECOND_BASE, SECOND_BASE+4, SECOND_BASE+2, SECOND_BASE+2+16, SECOND_BASE, SECOND_BASE+1,
//	(FIRST_BASE)%65,  (FIRST_BASE+1)%65,  (FIRST_BASE+3)%65, (FIRST_BASE+3+16)%65, (FIRST_BASE+4)%65, (FIRST_BASE+4+4)%65, (FIRST_BASE+3+3)%65, (FIRST_BASE+3+1+3)%65};
char *buf = "Hello, World!\x55\xaa";
char outbuf[16];

/* Oscillator setup variants (maybe more), values inside of chip ANDed to not allow by programming errors work it at higher speeds  */
/* WARNING! no chip temperature control limits, etc. It may self-fry and make fried chips with great ease :-) So if trying to overclock */
/* Do not place chip near flammable objects, provide adequate power protection and better wear eye protection ! */
/* Thermal runaway in this case could produce nice flames of chippy fries */

// Thermometer code from left to right - more ones ==> faster clock!

/* Test vectors to calculate (using address-translated loads) */
unsigned atrvec[] = {
0xb0e72d8e, 0x1dc5b862, 0xe9e7c4a6, 0x3050f1f5, 0x8a1a6b7e, 0x7ec384e8, 0x42c1c3fc, 0x8ed158a1, /* MIDSTATE */
0,0,0,0,0,0,0,0,
0x8a0bb7b7, 0x33af304f, 0x0b290c1a, 0xf0c4e61f, /* WDATA: hashMerleRoot[7], nTime, nBits, nNonce */

0x9c4dfdc0, 0xf055c9e1, 0xe60f079d, 0xeeada6da, 0xd459883d, 0xd8049a9d, 0xd49f9a96, 0x15972fed, /* MIDSTATE */
0,0,0,0,0,0,0,0,
0x048b2528, 0x7acb2d4f, 0x0b290c1a, 0xbe00084a, /* WDATA: hashMerleRoot[7], nTime, nBits, nNonce */

0x0317b3ea, 0x1d227d06, 0x3cca281e, 0xa6d0b9da, 0x1a359fe2, 0xa7287e27, 0x8b79c296, 0xc4d88274, /* MIDSTATE */
0,0,0,0,0,0,0,0,
0x328bcd4f, 0x75462d4f, 0x0b290c1a, 0x002c6dbc, /* WDATA: hashMerleRoot[7], nTime, nBits, nNonce */

0xac4e38b6, 0xba0e3b3b, 0x649ad6f8, 0xf72e4c02, 0x93be06fb, 0x366d1126, 0xf4aae554, 0x4ff19c5b, /* MIDSTATE */
0,0,0,0,0,0,0,0,
0x72698140, 0x3bd62b4f, 0x3fd40c1a, 0x801e43e9, /* WDATA: hashMerleRoot[7], nTime, nBits, nNonce */

0x9dbf91c9, 0x12e5066c, 0xf4184b87, 0x8060bc4d, 0x18f9c115, 0xf589d551, 0x0f7f18ae, 0x885aca59, /* MIDSTATE */
0,0,0,0,0,0,0,0,
0x6f3806c3, 0x41f82a4f, 0x3fd40c1a, 0x00334b39, /* WDATA: hashMerleRoot[7], nTime, nBits, nNonce */

};

#define rotrFixed(x,y) (((x) >> (y)) | ((x) << (32-(y))))
#define s0(x) (rotrFixed(x,7)^rotrFixed(x,18)^(x>>3))
#define s1(x) (rotrFixed(x,17)^rotrFixed(x,19)^(x>>10))
#define Ch(x,y,z) (z^(x&(y^z)))
#define Maj(x,y,z) (y^((x^y)&(y^z)))
#define S0(x) (rotrFixed(x,2)^rotrFixed(x,13)^rotrFixed(x,22))
#define S1(x) (rotrFixed(x,6)^rotrFixed(x,11)^rotrFixed(x,25))

/* SHA256 CONSTANTS */
static const unsigned SHA_K[64] = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
};

void t_print(struct timespec d_time) {
	printf(" %ds %.2fms\n", (int)d_time.tv_sec, (double)d_time.tv_nsec / 1000000.0);
}



struct  timespec t_add(struct  timespec  time1, struct  timespec  time2) {
    struct  timespec  result ;

    result.tv_sec = time1.tv_sec + time2.tv_sec ;
    result.tv_nsec = time1.tv_nsec + time2.tv_nsec ;
    if (result.tv_nsec >= 1000000000L) {
        result.tv_sec++ ;  result.tv_nsec = result.tv_nsec - 1000000000L ;
    }

    return (result) ;
}



struct timespec t_diff(struct timespec start, struct timespec end)
{
	struct timespec temp;
	if (end.tv_nsec < start.tv_nsec) {
		temp.tv_sec = end.tv_sec-start.tv_sec-1;
		temp.tv_nsec = 1000000000LU;
		temp.tv_nsec -= start.tv_nsec;
		temp.tv_nsec += end.tv_nsec;
	} else {
		temp.tv_sec = end.tv_sec-start.tv_sec;
		temp.tv_nsec = end.tv_nsec-start.tv_nsec;
	}
	return temp;
}

void ms3_compute(unsigned *p)
{
	unsigned a,b,c,d,e,f,g,h, ne, na,  i;

	a = p[0]; b = p[1]; c = p[2]; d = p[3]; e = p[4]; f = p[5]; g = p[6]; h = p[7];

	for (i = 0; i < 3; i++) {
		ne = p[i+16] + SHA_K[i] + h + Ch(e,f,g) + S1(e) + d;
		na = p[i+16] + SHA_K[i] + h + Ch(e,f,g) + S1(e) + S0(a) + Maj(a,b,c);
		d = c; c = b; b = a; a = na;
		h = g; g = f; f = e; e = ne;
	}

	p[15] = a; p[14] = b; p[13] = c; p[12] = d; p[11] = e; p[10] = f; p[9] = g; p[8] = h;
}

void send_conf() {
	config_reg(7,0); config_reg(8,0); config_reg(9,0); config_reg(10,0); config_reg(11,0);
	config_reg(6,0); /* disable OUTSLK */
	config_reg(4,1); /* Enable slow oscillator */
	config_reg(1,0); config_reg(2,0); config_reg(3,0);
	spi_emit_data(0x0100, (void*)counters, 16); /* Program counters correctly for rounds processing, here baby should start consuming power */
}

void send_init() {
	/* Prepare internal buffers */
	/* PREPARE BUFFERS (INITIAL PROGRAMMING) */
	unsigned w[16];
	unsigned atrvec[] = {
		0xb0e72d8e, 0x1dc5b862, 0xe9e7c4a6, 0x3050f1f5, 0x8a1a6b7e, 0x7ec384e8, 0x42c1c3fc, 0x8ed158a1, /* MIDSTATE */
		0,0,0,0,0,0,0,0,
		0x8a0bb7b7, 0x33af304f, 0x0b290c1a, 0xf0c4e61f, /* WDATA: hashMerleRoot[7], nTime, nBits, nNonce */
	};

	ms3_compute(&atrvec[0]);
	memset(&w, 0, sizeof(w)); w[3] = 0xffffffff; w[4] = 0x80000000; w[15] = 0x00000280;
	spi_emit_data(0x1000, (void*)w, 16*4);
	spi_emit_data(0x1400, (void*)w,  8*4);
	memset(w, 0, sizeof(w)); w[0] = 0x80000000; w[7] = 0x100;
	spi_emit_data(0x1900, (void*)&w[0],8*4); /* Prepare MS and W buffers! */
	spi_emit_data(0x3000, (void*)&atrvec[0], 19*4);
}

void set_freq(int bits) {
	uint64_t freq;
	unsigned char *osc6;
	int i;

	osc6 = (unsigned char *)&freq;
	freq = (1ULL << bits) - 1ULL;

	spi_emit_data(0x6000, (void*)osc6, 8); /* Program internal on-die slow oscillator frequency */
	config_reg(4,1); /* Enable slow oscillator */
}

void send_reinit(int slot, int chip_n, int n) {
	spi_clear_buf();
	spi_emit_break();
	spi_emit_fasync(chip_n);
	set_freq(n);
	send_conf();
	send_init();
	select_bank(slot);
	spi_txrx(spi_gettxbuf(), spi_getrxbuf(), spi_getbufsz());
	clear_bank_selection();
}

void send_shutdown(int slot, int chip_n) {
	spi_clear_buf();
	spi_emit_break();
	spi_emit_fasync(chip_n);
	config_reg(4,0); /* Disable slow oscillator */
	select_bank(slot);
	spi_txrx(spi_gettxbuf(), spi_getrxbuf(), spi_getbufsz());
	clear_bank_selection();
}

void send_freq(int slot, int chip_n, int bits) {
	spi_clear_buf();
	spi_emit_break();
	spi_emit_fasync(chip_n);
	set_freq(bits);
	select_bank(slot);
	spi_txrx(spi_gettxbuf(), spi_getrxbuf(), spi_getbufsz());
	clear_bank_selection();
}

unsigned int c_diff(unsigned ocounter, unsigned counter) {
	return counter >  ocounter ? counter - ocounter : (0x003FFFFF - ocounter) + counter;
}

int get_counter(unsigned int *newbuf, unsigned int *oldbuf) {
	int j;
	unsigned counter;
	for(j = 0; j < 16; j++) {
		if (newbuf[j] != oldbuf[j]) {
			int counter = decnonce(newbuf[j]);
			if ((counter & 0xFFC00000) == 0xdf800000) {
				counter -= 0xdf800000;
				return counter;
			}
		}
	}
	return 0;
}

int get_diff(unsigned int *newbuf, unsigned int *oldbuf) {
		int j;
		unsigned counter = 0;
		for(j = 0; j < 16; j++) {
				if (newbuf[j] != oldbuf[j]) {
						counter++;
				}
		}
		return counter;
}

int detect_chip(int chip_n) {
	int i;
	unsigned newbuf[17], oldbuf[17];
	unsigned ocounter;
	int odiff;
	struct timespec t1, t2, td;

	memset(newbuf, 0, 17 * 4);
	memset(oldbuf, 0, 17 * 4);


	spi_clear_buf();
	spi_emit_break(); /* First we want to break chain! Otherwise we'll get all of traffic bounced to output */
	spi_emit_fasync(chip_n);
	set_freq(52);  //54 - 3F, 53 - 1F
	send_conf();
	send_init();
	spi_txrx(spi_gettxbuf(), spi_getrxbuf(), spi_getbufsz());

	ocounter = 0;
	for (i = 0; i < BITFURY_DETECT_TRIES; i++) {
		int j;
		int counter;

		spi_clear_buf();
		spi_emit_break();
		spi_emit_fasync(chip_n);
		spi_emit_data(0x3000, (void*)&atrvec[0], 19*4);
		spi_txrx(spi_gettxbuf(), spi_getrxbuf(), spi_getbufsz());
		memcpy(newbuf, spi_getrxbuf() + 4 + chip_n, 17*4);

		clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &t1);
		counter = get_counter(newbuf, oldbuf);
		if (ocounter) {
			unsigned int cdiff = c_diff(ocounter, counter);
			unsigned per_ms;

			td = t_diff(t2, t1);
			per_ms = cdiff / (td.tv_nsec / 1000);
			if (cdiff > 5000 && cdiff < 100000 && odiff > 5000 && odiff < 100000)
				return 1;
			odiff = cdiff;
		}
		ocounter = counter;
		t2 = t1;
		if (newbuf[16] != 0 && newbuf[16] != 0xFFFFFFFF) {
			return 0;
		}
		nmsleep(BITFURY_REFRESH_DELAY / 10);
		memcpy(oldbuf, newbuf, 17 * 4);
	}
	return 0;
}

int libbitfury_detectChips(struct bitfury_device *devices) {
	int n = 0;
	int i;
	static slot_on[BITFURY_MAXBANKS];
	struct timespec t1, t2;

#ifndef BITFURY_ARES
	if (tm_i2c_init() < 0) {
		printf("I2C init error\n");
		return(1);
	}
#endif

	ms3_compute(&atrvec[0]);
	ms3_compute(&atrvec[20]);
	ms3_compute(&atrvec[40]);

	gpio_init();
	banks_init();
	spi_init();

	for (i = 0; i < BITFURY_MAXBANKS; i++) {
		slot_on[i] = 0;
	}

	clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &t1);
	for (i = 0; i < BITFURY_MAXBANKS; i++) {
		int slot_detected = detect_bank(i) != -1;
		slot_on[i] = slot_detected;
		clear_bank_selection();
		nmsleep(1);
	}

	for (i = 0; i < BITFURY_MAXBANKS; i++) {
//		if (slot_on[i]) {
			int chip_n = 0;
			int chip_detected;
			select_bank(i);
			do {
				chip_detected = detect_chip(chip_n);
				//chip_detected = n < 18;
				if (chip_detected) {
					applog(LOG_WARNING, "BITFURY slot: %d, chip #%d detected", i, n);
					devices[n].slot = i;
					devices[n].fasync = chip_n;
					n++;
					chip_n++;
				}
			} while (chip_detected);
			clear_bank_selection();
//		}
	}

	clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &t2);

	return n; //!!!
	//return 1;
}

int libbitfury_shutdownChips(struct bitfury_device *devices, int chip_n) {
	int i;
	for (i = 0; i < chip_n; i++) {
		send_shutdown(devices[i].slot, devices[i].fasync);
	}
#ifndef BITFURY_ARES
	tm_i2c_close();
#endif
}

unsigned decnonce(unsigned in)
{
	unsigned out;

	/* First part load */
	out = (in & 0xFF) << 24; in >>= 8;

	/* Byte reversal */
	in = (((in & 0xaaaaaaaa) >> 1) | ((in & 0x55555555) << 1));
	in = (((in & 0xcccccccc) >> 2) | ((in & 0x33333333) << 2));
	in = (((in & 0xf0f0f0f0) >> 4) | ((in & 0x0f0f0f0f) << 4));

	out |= (in >> 2)&0x3FFFFF;

	/* Extraction */
	if (in & 1) out |= (1 << 23);
	if (in & 2) out |= (1 << 22);

	out -= 0x800004;
	return out;
}

int rehash(unsigned char *midstate, unsigned m7,
			unsigned ntime, unsigned nbits, unsigned nnonce) {
	unsigned char in[16];
	unsigned char hash1[32];
	unsigned int *in32 = (unsigned int *)in;
	unsigned char *hex;
	unsigned int *mid32 = (unsigned int *)midstate;
	unsigned out32[8];
	unsigned char *out = (unsigned char *) out32;
	static unsigned history[512];
	static unsigned history_p;
	int i;
	sha2_context ctx;


	memset( &ctx, 0, sizeof( sha2_context ) );
	memcpy(ctx.state, mid32, 8*4);
	ctx.total[0] = 64;
	ctx.total[1] = 0;

	nnonce = bswap_32(nnonce);
	in32[0] = bswap_32(m7);
	in32[1] = bswap_32(ntime);
	in32[2] = bswap_32(nbits);
	in32[3] = nnonce;

	sha2_update(&ctx, in, 16);
	sha2_finish(&ctx, out);
	sha2(out, 32, out);

	if (out32[7] == 0) {
//		hex = bin2hex(midstate, 32);
//		hex = bin2hex(out, 32);
//		applog(LOG_INFO, "! MS0: %08x, m7: %08x, ntime: %08x, nbits: %08x, nnonce: %08x\n\t\t\t out: %s\n", mid32[0], m7, ntime, nbits, nnonce, hex);
//		history[history_p] = nnonce;
//		history_p++; history_p &= 512 - 1;
		return 1;
	}
	return 0;
}

void work_to_payload(struct bitfury_payload *p, struct work *w) {
	unsigned char flipped_data[80];

	memset(p, 0, sizeof(struct bitfury_payload));
	flip80(flipped_data, w->data);

	memcpy(p->midstate, w->midstate, 32);
	p->m7 = bswap_32(*(unsigned *)(flipped_data + 64));
	p->ntime = bswap_32(*(unsigned *)(flipped_data + 68));
	p->nbits = bswap_32(*(unsigned *)(flipped_data + 72));
}

int libbitfury_sendHashData(struct thr_info *thr, struct bitfury_device *bf, int chip_n) {
	int chip_id;
	int buf_diff;
	static unsigned second_run;

	for (chip_id = 0; chip_id < chip_n; chip_id++) {
		unsigned char *hexstr;
		struct bitfury_device *d = bf + chip_id;
		unsigned *newbuf = d->newbuf;
		unsigned *oldbuf = d->oldbuf;
		struct bitfury_payload *p = &(d->payload);
		struct bitfury_payload *op = &(d->opayload);
		struct bitfury_payload *o2p = &(d->o2payload);
		struct timespec d_time;
		struct timespec time;
		int smart = 0;
		int i;
		int chip = d->fasync;
		int slot = d->slot;

		memcpy(atrvec, p, 20*4);
		ms3_compute(atrvec);

		clock_gettime(CLOCK_REALTIME, &(time));

		if (!second_run) {
			d->predict2 = d->predict1 = time;
			d->counter1 = d->counter2 = 0;
			d->req2_done = 0;
		};

		d_time = t_diff(time, d->predict1);
		if (d_time.tv_sec < 0 && (d->req2_done || !smart)) {
			d->otimer1 = d->timer1;
			d->timer1 = time;
			d->ocounter1 = d->counter1;
			if (d->osc6_bits != d->osc6_req) {
                          send_freq(d->slot, d->fasync, d->osc6_req);
                          d->osc6_bits = d->osc6_req;
                        }
			/* Programming next value */
			select_bank(slot);
			spi_clear_buf(); spi_emit_break();
			spi_emit_fasync(chip);
			spi_emit_data(0x3000, (void*)&atrvec[0], 19*4);
			if (smart) {
				config_reg(3,0);
			}
			clock_gettime(CLOCK_REALTIME, &(time));
			d_time = t_diff(time, d->predict1);
			spi_txrx(spi_gettxbuf(), spi_getrxbuf(), spi_getbufsz());
			memcpy(newbuf, spi_getrxbuf()+4 + chip, 17*4);
			d->counter1 = get_counter(newbuf, oldbuf);
			buf_diff = get_diff(newbuf, oldbuf);
			if (buf_diff > 4 || (d->counter1 > 0 && d->counter1 < 0x00400000 / 2)) {
				if (buf_diff > 4) {
//					printf("AAA        chip_id: %d, buf_diff: %d, counter: %08x\n", chip_id, buf_diff, d->counter1);
					memcpy(atrvec, p, 20*4);
					ms3_compute(atrvec);
					spi_clear_buf(); spi_emit_break();
					spi_emit_fasync(chip);
					spi_emit_data(0x3000, (void*)&atrvec[0], 19*4);
					clock_gettime(CLOCK_REALTIME, &(time));
					d_time = t_diff(time, d->predict1);
					spi_txrx(spi_gettxbuf(), spi_getrxbuf(), spi_getbufsz());
					memcpy(newbuf, spi_getrxbuf()+4 + chip, 17*4);
					buf_diff = get_diff(newbuf, oldbuf);
					d->counter1 = get_counter(newbuf, oldbuf);
//					printf("AAA _222__ chip_id: %d, buf_diff: %d, counter: %08x\n", chip_id, buf_diff, d->counter1);
				}
			}

			clear_bank_selection();

			d->job_switched = newbuf[16] != oldbuf[16];

			int i;
			int results_num = 0;
			int found = 0;
			unsigned * results = d->results;

			d->old_nonce = 0;
			d->future_nonce = 0;
			for (i = 0; i < 16; i++) {
				if (oldbuf[i] != newbuf[i] && op && o2p) {
					unsigned pn; //possible nonce
					unsigned int s = 0; //TODO zero may be solution
					unsigned int old_f = 0;
					if ((newbuf[i] & 0xFF) == 0xE0)
						continue;
					pn = decnonce(newbuf[i]);
					if(rehash(op->midstate, op->m7, op->ntime, op->nbits, pn)) s = pn;
					else if(rehash(op->midstate, op->m7, op->ntime, op->nbits, pn-0x00400000)) s = pn - 0x00400000;
					else if(rehash(op->midstate, op->m7, op->ntime, op->nbits, pn-0x00800000)) s = pn - 0x00800000;
					else if(rehash(op->midstate, op->m7, op->ntime, op->nbits, pn+0x02800000)) s = pn + 0x02800000;
					else if(rehash(op->midstate, op->m7, op->ntime, op->nbits, pn+0x02C00000)) s = pn + 0x02C00000;
					else if(rehash(op->midstate, op->m7, op->ntime, op->nbits, pn+0x00400000)) s = pn + 0x00400000;
					if (s) {
						results[results_num++] = bswap_32(s);
						found++;
					}

					s = 0;
					pn = decnonce(newbuf[i]);
					if(rehash(o2p->midstate, o2p->m7, o2p->ntime, o2p->nbits, pn)) s = pn;
					else if(rehash(o2p->midstate, o2p->m7, o2p->ntime, o2p->nbits, pn-0x00400000)) s = pn - 0x00400000;
					else if(rehash(o2p->midstate, o2p->m7, o2p->ntime, o2p->nbits, pn-0x00800000)) s = pn - 0x00800000;
					else if(rehash(o2p->midstate, o2p->m7, o2p->ntime, o2p->nbits, pn+0x02800000)) s = pn + 0x02800000;
					else if(rehash(o2p->midstate, o2p->m7, o2p->ntime, o2p->nbits, pn+0x02C00000)) s = pn + 0x02C00000;
					else if(rehash(o2p->midstate, o2p->m7, o2p->ntime, o2p->nbits, pn+0x00400000)) s = pn + 0x00400000;
					if (s) {
						d->old_nonce = bswap_32(s);
						found++;
					}

					s = 0;
					pn = decnonce(newbuf[i]);
					if(rehash(p->midstate, p->m7, p->ntime, p->nbits, pn)) s = pn;
					else if(rehash(p->midstate, p->m7, p->ntime, p->nbits, pn-0x00400000)) s = pn - 0x00400000;
					else if(rehash(p->midstate, p->m7, p->ntime, p->nbits, pn-0x00800000)) s = pn - 0x00800000;
					else if(rehash(p->midstate, p->m7, p->ntime, p->nbits, pn+0x02800000)) s = pn + 0x02800000;
					else if(rehash(p->midstate, p->m7, p->ntime, p->nbits, pn+0x02C00000)) s = pn + 0x02C00000;
					else if(rehash(p->midstate, p->m7, p->ntime, p->nbits, pn+0x00400000)) s = pn + 0x00400000;
					if (s) {
						d->future_nonce = bswap_32(s);
						found++;
					}
					if (!found) {
						//printf("AAA Strange: %08x, chip_id: %d\n", pn, chip_id);
						d->hw_errors++;
						inc_hw_errors(thr);
					}
				}
			}
			d->results_n = results_num;

			if (smart) {
				d_time = t_diff(d->timer2, d->timer1);
			} else {
				d_time = t_diff(d->otimer1, d->timer1);
			}
			d->counter1 = get_counter(newbuf, oldbuf);
			if (d->counter2 || !smart) {
				int shift;
				int cycles;
				int req1_cycles;
				long long unsigned int period;
				double ns;
				unsigned full_cycles, half_cycles;
				double full_delay, half_delay;
				long long unsigned delta;
				struct timespec t_delta;
				double mhz;
				int ccase;

				shift = 800000;
				if (smart) {
					cycles = d->counter1 < d->counter2 ? 0x00400000 - d->counter2 + d->counter1 : d->counter1 - d->counter2; // + 0x003FFFFF;
				} else {
					if (d->counter1 > (0x00400000 - shift * 2) && d->ocounter1 > (0x00400000 - shift)) {
						cycles = 0x00400000 - d->ocounter1 + d->counter1; // + 0x003FFFFF;
						ccase = 1;
					} else {
						cycles = d->counter1 > d->ocounter1 ? d->counter1 - d->ocounter1 : 0x00400000 - d->ocounter1 + d->counter1;
						ccase = 2;
					}
				}
				req1_cycles = 0x003FFFFF - d->counter1;
				period = (long long unsigned int)d_time.tv_sec * 1000000000ULL + (long long unsigned int)d_time.tv_nsec;
				ns = (double)period / (double)(cycles);
				mhz = 1.0 / ns * 65.0 * 1000.0;

				if (d->counter1 > 0 && d->counter1 < 0x001FFFFF) {
					//printf("//AAA chip_id %2d: %llu ms, req1_cycles: %08u,  counter1: %08d, ocounter1: %08d, counter2: %08d, cycles: %08d, ns: %.2f, mhz: %.2f \n", chip_id, period / 1000000ULL, req1_cycles, d->counter1, d->ocounter1, d->counter2, cycles, ns, mhz);
				}
				if (ns > 2000.0 || ns < 20) {
					//printf("AAA %d!Stupid ns chip_id %2d: %llu ms, req1_cycles: %08u,  counter1: %08d, ocounter1: %08d, counter2: %08d, cycles: %08d, ns: %.2f, mhz: %.2f \n", ccase, chip_id, period / 1000000ULL, req1_cycles, d->counter1, d->ocounter1, d->counter2, cycles, ns, mhz);
					ns = 200.0;
				} else {
					d->ns = ns;
					d->mhz = mhz;
				}

				if (smart) {
					half_cycles = req1_cycles + shift;
					full_cycles = 0x003FFFFF - 2 * shift;
				} else {
					half_cycles = 0;
					full_cycles = req1_cycles > shift ? req1_cycles - shift : req1_cycles + 0x00400000 - shift;
				}
				half_delay = (double)half_cycles * ns * (1 +0.92);
				full_delay = (double)full_cycles * ns;
				delta = (long long unsigned)(full_delay + half_delay);
				t_delta.tv_sec = delta / 1000000000ULL;
				t_delta.tv_nsec = delta - t_delta.tv_sec * 1000000000ULL;
				d->predict1 = t_add(time, t_delta);

				if (smart) {
					half_cycles = req1_cycles + shift;
					full_cycles = 0;
				} else {
					full_cycles = req1_cycles + shift;
				}
				half_delay = (double)half_cycles * ns * (1 + 0.92);
				full_delay = (double)full_cycles * ns;
				delta = (long long unsigned)(full_delay + half_delay);

				t_delta.tv_sec = delta / 1000000000ULL;
				t_delta.tv_nsec = delta - t_delta.tv_sec * 1000000000ULL;
				d->predict2 = t_add(time, t_delta);
				d->req2_done = 0; d->req1_done = 0;
			}

			if (d->job_switched) {
				memcpy(o2p, op, sizeof(struct bitfury_payload));
				memcpy(op, p, sizeof(struct bitfury_payload));
				memcpy(oldbuf, newbuf, 17 * 4);
			}
		}

		clock_gettime(CLOCK_REALTIME, &(time));
		d_time = t_diff(time, d->predict2);
		if (d_time.tv_sec < 0 && !d->req2_done) {
			if(smart) {
				d->otimer2 = d->timer2;
				d->timer2 = time;
				spi_clear_buf();
				spi_emit_break();
				spi_emit_fasync(chip);
				spi_emit_data(0x3000, (void*)&atrvec[0], 19*4);
				if (smart) {
					config_reg(3,1);
				}
				select_bank(slot);
				spi_txrx(spi_gettxbuf(), spi_getrxbuf(), spi_getbufsz());
				clear_bank_selection();
				memcpy(newbuf, spi_getrxbuf()+4 + chip, 17*4);
				d->counter2 = get_counter(newbuf, oldbuf);

				d->req2_done = 1;
			} else {
				d->req2_done = 1;
			}
		}
	}
	second_run = 1;

	return;
}

int libbitfury_readHashData(unsigned int *res) {
	return 0;
}

