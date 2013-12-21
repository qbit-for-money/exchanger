#include "gpio.h"
#include "bitfury-config.h"
#include "libbitfury-config.h"

#ifdef BITFURY_QBIT
// simple decoder on address bus
// http://elinux.org/Rpi_Low-level_peripherals

#define A0_PORT_MASK 0x00040000 // GPIO18
#define A1_PORT_MASK 0x00800000 // GPIO23
#define A2_PORT_MASK 0x01000000 // GPIO24
#define A3_PORT_MASK 0x02000000 // GPIO25
#define A_BUS_PORT_MASK (A0_PORT_MASK | A1_PORT_MASK | A2_PORT_MASK | A3_PORT_MASK)

static const unsigned BANK_NUM_TO_PORT_MASK_MAP[BITFURY_MAXBANKS] = {
	0x00000000, A0_PORT_MASK
};
#endif

void clear_bank_selection(void);

void banks_init(void)
{
	clear_bank_selection();
}

int detect_bank(unsigned char bank_num)
{
	//return tm_i2c_detect(bank_num);
	return 0;
}

void select_bank(unsigned char bank_num)
{
	//tm_i2c_set_oe(bank_num);
#ifdef BITFURY_QBIT
	clear_bank_selection();
	GPIO_SET = BANK_NUM_TO_PORT_MASK_MAP[bank_num];
#endif
}

void clear_bank_selection(void)
{
	//tm_i2c_clear_oe();
#ifdef BITFURY_QBIT
	GPIO_CLR = A_BUS_PORT_MASK;
#endif
}