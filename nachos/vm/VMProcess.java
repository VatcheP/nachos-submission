package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
	/**
	 * Allocate a new process.
	 */
	public VMProcess() {
		super();
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
		super.saveState();
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		super.restoreState();
	}

	/**
	 * Initializes page tables for this process so that the executable can be
	 * demand-paged.
	 * 
	 * @return <tt>true</tt> if successful.
	 */
	protected boolean loadSections() {
 		pageTable = new TranslationEntry[numPages];

        for (int vpn = 0; vpn < numPages; vpn++) {
        	pageTable[vpn] = new TranslationEntry(vpn, -1, false, false, false, false);
    	}

    		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		super.unloadSections();
	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
	 * . The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause the user exception that occurred.
	 */
	public void handleException(int cause) {
    	Processor processor = Machine.processor();

    	switch (cause) {
    	case Processor.exceptionPageFault:
        	int badVaddr = processor.readRegister(Processor.regBadVAddr);
        	handlePageFault(badVaddr);
		break;

    	default:
        	super.handleException(cause);
        	break;
    		}
	}
	
	private void handlePageFault(int vaddr) {
    	int vpn = Processor.pageFromAddress(vaddr);

    	if (vpn < 0 || vpn >= numPages) {
        	Lib.debug(dbgVM, "invalid page fault vpn=" + vpn);
        	return;
    	}

    	int ppn;

	UserKernel.freePagesLock.acquire();
	
	if (UserKernel.freePages.isEmpty()) {
    		UserKernel.freePagesLock.release();
    		Lib.assertTrue(false);
    		return;
	}	

	ppn = UserKernel.freePages.removeFirst();

	UserKernel.freePagesLock.release();

    	byte[] memory = Machine.processor().getMemory();

    	// zero-fill by default for stack/args pages
    	for (int i = 0; i < pageSize; i++) {
        	memory[ppn * pageSize + i] = 0;
    	}

    	boolean loadedFromCoff = false;

    	for (int s = 0; s < coff.getNumSections(); s++) {
        	CoffSection section = coff.getSection(s);

        int firstVPN = section.getFirstVPN();
        int length = section.getLength();

        if (vpn >= firstVPN && vpn < firstVPN + length) {
            	section.loadPage(vpn - firstVPN, ppn);
            	
		pageTable[vpn].readOnly = section.isReadOnly();
            	
		loadedFromCoff = true;
            	
		break;
        	}
    	}

    		pageTable[vpn].ppn = ppn;
    		pageTable[vpn].valid = true;
    		pageTable[vpn].used = false;
    		pageTable[vpn].dirty = false;
	}

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';
}
