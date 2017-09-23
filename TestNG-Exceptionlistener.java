import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

import com.vmware.g11n.log.GLogger;
import com.xhive.util.interfaces.StringWriter;

public class ExceptionListener extends TestListenerAdapter implements IInvokedMethodListener{
        //This 'GLogger' is my log class based on Log4j, you can use your own
	private static GLogger log = GLogger.getInstance(ExceptionListener.class.getName());
	public void beforeInvocation(IInvokedMethod arg0, ITestResult arg1) {}
	
	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult result) {
		Reporter.setCurrentTestResult(result);
		if (method.isTestMethod()) {
			List<Throwable> verificationFailures = log.getVerificationFailures();

			//if there are verification failures...
			if (verificationFailures.size() > 0) {
				//set the test to failed
				result.setStatus(ITestResult.FAILURE);
				result.setThrowable(verificationFailures.get(0));
				log.cleanVerificationFailures();
			}
		}
	}

	@Override
	public void onTestFailure(ITestResult testResult) {
		Throwable exceptionThrowable = testResult.getThrowable();
		if (exceptionThrowable!=null && !isAssertionError(exceptionThrowable)) {
			log.handleException(exceptionThrowable);//You can call your own exception handler
		}
	}

	@Override
	public void onConfigurationFailure(ITestResult testResult) {
		Throwable exceptionThrowable = testResult.getThrowable();
		if (exceptionThrowable!=null) {
			log.handleException(exceptionThrowable);//You can call your own exception handler
		}
		if (testResult.getMethod().getMethodName().equals("suiteSetUp")) {
			try {
				TestBase.suiteCleanUp();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
    public void onTestSkipped(ITestResult testResult) {
	    Throwable exceptionThrowable = testResult.getThrowable();
        if (exceptionThrowable!=null && !isAssertionError(exceptionThrowable)) {
        	log.handleException(exceptionThrowable);//You can call your own exception handler
        }
    }

	private boolean isAssertionError(Throwable throwed) {
		String message = stackTraceToString(throwed);
		return message.startsWith("java.lang.AssertionError:");
	}
	
	public String stackTraceToString(Throwable throwable) {
		String printString = "";
		StringWriter sWriter = new StringWriter();
		PrintWriter pWriter = new PrintWriter(sWriter);
		try {
			throwable.printStackTrace(pWriter);
			printString = sWriter.toString();
			pWriter.flush();
			pWriter.close();
			sWriter.flush();
			sWriter.close();
		}
		catch (IOException e) {
			log.log("Stream throwable stack trace failed.");
			e.printStackTrace();
		}
		return printString;
	}
}