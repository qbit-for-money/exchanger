#ifdef __BANK_H__
#define __BANK_H__

extern void banks_init(void);
extern int detect_bank(unsigned char bank_num);
extern void select_bank(unsigned char bank_num);
extern void clear_bank_selection(void);

#endif