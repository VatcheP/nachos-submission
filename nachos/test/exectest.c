#include "syscall.h"

int main() {
    int pid = exec("halt.coff", 0, 0);

    if (pid < 0) {
        exit(1);
    }

    exit(0);
}
