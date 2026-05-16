#include "syscall.h"

int main() {
    int fd = creat("testfile.txt");

    if (fd < 0) {
        exit(1);
    }

    exit(0);
}
