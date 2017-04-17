package avalon.main;

import avalon.extend.Recorder;
import avalon.extend.Scheduler;
import avalon.extend.ShowMsg;
import avalon.group.MainGroupMessageHandler;
import avalon.tool.ConfigSystem;
import avalon.tool.DelayResponse;
import avalon.tool.Responder;
import avalon.tool.SQLiteDatabaseOperator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static avalon.tool.ConstantPool.Address.webqq;
import static avalon.tool.ConstantPool.Address.wechat;

/**
 * Created by Eldath on 2017/1/28 0028.
 *
 * @author Eldath
 */
public class MainServer {
    private static final Logger logger = LoggerFactory.getLogger(MainServer.class);
    private static long[] followGroup = MainGroupMessageHandler.getFollowGroup();

    static class atShutdownDo extends Thread {
        @Override
        public void run() {
            MainServer.logger.info("Do cleaning job...");
            Recorder.getInstance().flushNow();
            SQLiteDatabaseOperator.getInstance().closeResource();
            try {
                new URL(webqq + "/openqq/stop_client").openStream();
                new URL(wechat + "/openwx/stop_client").openStream();
            } catch (IOException ignored) {
            }
            for (long thisFollowFollow : followGroup)
                Responder.sendToGroup(thisFollowFollow, "服务已经停止。");
        }
    }

    public static void main(String[] args) throws Exception {
        // 线程池
        ScheduledThreadPoolExecutor poolExecutor = new ScheduledThreadPoolExecutor(1);
        poolExecutor.scheduleWithFixedDelay(new Scheduler(), 5, 5, TimeUnit.SECONDS);
        // 初始化Task
        new ShowMsg();
        // 关车钩子
        Runtime.getRuntime().addShutdownHook(new atShutdownDo());
        InetSocketAddress address;
        final String addressString = ((String) ConfigSystem.getInstance()
                .getConfig("Mojo-Webqq_POST_API_Address")).replace("http://", "");
        if (!addressString.contains(":"))
            address = new InetSocketAddress(addressString, 80);
        else {
            final String[] addressStringArray = addressString.split(":");
            address = new InetSocketAddress(addressStringArray[0].replace("//", ""),
                    Integer.parseInt(addressStringArray[1]));
        }
        //
        Server server = new Server(address);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);
        server.setStopAtShutdown(true);
        context.addServlet(new ServletHolder(new MainServlet()), "/post_api");
        server.start();
        //
        logger.info("Is server on (yes or no): ");
        Scanner scanner = new Scanner(System.in);
        String isOn = scanner.nextLine();
        if ("yes".equals(isOn.toLowerCase()))
            for (long thisFollowGroup : followGroup)
                Responder.sendToGroup(thisFollowGroup, "Avalon已经上线。");
        else logger.info("Cancel send login message.");
        DelayResponse delayResponse = new DelayResponse();
        delayResponse.start();
        logger.info("DelayResponse thread is loaded.");
        logger.info("Server now running!");
        logger.error("IMPORTANCE: Please exit this system by pressed Ctrl-C, DO NOT close this window directly!");
        server.join();
    }
}