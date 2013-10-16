#include <sys/mman.h>
#include <stdint.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/types.h>
#include <signal.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>
#include <sys/stat.h>

volatile unsigned *gpio;

void gpio_init(void)
{
	int mem_fd;
	mem_fd = open("/dev/mem",O_RDWR|O_SYNC);
	if (mem_fd < 0) {
		perror("FATAL, /dev/mem trouble (must be roor)");
		exit(1);
	}
	gpio = mmap(0,4096,PROT_READ|PROT_WRITE,MAP_SHARED,mem_fd,0x20200000);
	if (gpio == MAP_FAILED) {
		perror("FATAL, gpio mmap trouble (must be root)");
		close(mem_fd);
		exit(1);
	}
}