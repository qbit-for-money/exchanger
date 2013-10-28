#include "gpio.h"

//#define BITFURY_MAXBANKS 10
#define BITFURY_MAXBANKS 1

// http://elinux.org/Rpi_Low-level_peripherals
static const unsigned BANK_NUM_TO_PORT_NUM_MAP[BITFURY_MAXBANKS] = {
	18/*, 23, 24, 25,			// top
#ifdef RPI_PIN_NAME_REV2
	2, 3, 4, 17, 27, 22		// bottom
#else
	0, 1, 4, 17, 21, 22		// bottom
#endif*/
};

static unsigned ALL_BANK_PORT_NUMS = 0;

void clear_bank_selection(void);

void banks_init(void)
{
	unsigned i;
	ALL_BANK_PORT_NUMS = 0;
	for (i = 0; i < BITFURY_MAXBANKS; i++) {
		ALL_BANK_PORT_NUMS |= (1 << BANK_NUM_TO_PORT_NUM_MAP[i]);
		INP_GPIO(BANK_NUM_TO_PORT_NUM_MAP[i]); OUT_GPIO(BANK_NUM_TO_PORT_NUM_MAP[i]);
	}
	clear_bank_selection();
}

int detect_bank(unsigned char bank_num)
{
	return 0;
}

void select_bank(unsigned char bank_num)
{
	clear_bank_selection();
	GPIO_SET = (1 << BANK_NUM_TO_PORT_NUM_MAP[bank_num]);
}

void clear_bank_selection(void)
{
	GPIO_CLR = ALL_BANK_PORT_NUMS;
}