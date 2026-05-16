#include "syscall.h"

int main() {
    int fd;

    fd = creat("delete.txt");

    if (fd < 0) {
        exit(1);
    }

    if (unlink("delete.txt") < 0) {
        exit(2);
    }

    exit(0);
}
