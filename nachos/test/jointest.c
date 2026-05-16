#include "syscall.h"

int status;

int main() {
    int pid = exec("child.coff", 0, 0);

    if (pid < 0)
        exit(1);

    int result = join(pid, &status);

    if (result != 1)
        exit(2);

    if (status != 42)
        exit(3);

    exit(0);
}
