#include "gpio.h"

#define BITFURY_MAXBANKS 10

static const unsigned BANK_NUM_TO_PORT_NUM_MAP[BITFURY_MAXBANKS] = {
	0x00000000,
	0x00000000,
	0x00000000,
	0x00000000,
	0x00000000,
	0x00000000,
	0x00000000,
	0x00000000,
	0x00000000,
	0x00000000
};
static unsigned ALL_BANK_PORT_NUMS = 0;

void banks_init(void)
{
	unsigned i;
	for (i = 0; i < BITFURY_MAXBANKS; i++) {
		ALL_BANK_PORT_NUMS |= BANK_NUM_TO_PORT_NUM_MAP[i];
		INP_GPIO(BANK_NUM_TO_PORT_NUM_MAP[i]); OUT_GPIO(BANK_NUM_TO_PORT_NUM_MAP[i]);
	}
}

int detect_bank(unsigned char bank_num)
{
	return 0;
}

void clear_bank_selection(void)
{
	GPIO_CLR = ALL_BANK_PORT_NUMS;
}

void select_bank(unsigned char bank_num)
{
	clear_bank_selection();
	GPIO_SET = BANK_NUM_TO_PORT_NUM_MAP[bank_num];
}