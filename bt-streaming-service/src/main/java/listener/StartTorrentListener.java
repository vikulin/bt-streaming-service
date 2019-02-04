package listener;
import javax.servlet.ServletContextEvent;

import org.hyperborian.bt.service.TorrentSessionStateService;



public class StartTorrentListener implements javax.servlet.ServletContextListener {
	
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		new TorrentSessionStateService();
	}
	
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}

}
