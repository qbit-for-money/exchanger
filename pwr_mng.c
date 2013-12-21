#include "gpio.h"
#include "bitfury-config.h"

#ifdef BITFURY_QBIT
// http://elinux.org/Rpi_Low-level_peripherals

#define PWR2_PORT_MASK 0x00020000 // GPIO17
#define PWR1_PORT_MASK 0x08000000 // GPIO27
#define PWR0_PORT_MASK 0x00400000 // GPIO22
#define PWR_BUS_PORT_MASK (PWR2_PORT_MASK | PWR1_PORT_MASK | PWR0_PORT_MASK)

#endif

void pwr_level_init(void)
{
#ifdef BITFURY_QBIT
	GPIO_CLR = PWR_BUS_PORT_MASK;
#endif
}