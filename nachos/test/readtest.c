#include "syscall.h"

char buffer[32];

int main() {

    int fd;

    fd = creat("readfile.txt");

    if (fd < 0)
        exit(1);

    write(fd, "hello world", 11);

    close(fd);

    fd = open("readfile.txt");

    if (fd < 0)
        exit(2);

    int n = read(fd, buffer, 11);

    if (n < 0)
        exit(3);

    write(1, buffer, 11);

    exit(0);
}
