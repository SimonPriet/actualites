package net.atos.entng.actualites;

import net.atos.entng.actualites.controllers.CommentController;
import net.atos.entng.actualites.controllers.InfoController;
import net.atos.entng.actualites.controllers.ThreadController;
import net.atos.entng.actualites.controllers.DisplayController;
import net.atos.entng.actualites.services.impl.ActualitesRepositoryEvents;

import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.share.impl.SqlShareService;
import org.entcore.common.sql.SqlConf;
import org.entcore.common.sql.SqlConfs;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonArray;

public class Actualites extends BaseServer {
	public static final String THREAD_RESOURCE_ID = "threadid";
	public final static String THREAD_TABLE = "thread";
	public final static String THREAD_SHARE_TABLE = "thread_shares";

	public static final String INFO_RESOURCE_ID = "infoid";
	public final static String INFO_TABLE = "info";
	public final static String INFO_SHARE_TABLE = "info_shares";

	public final static String COMMENT_TABLE = "comment";

	@Override
	public void start() {
		super.start();
		final EventBus eb = getEventBus(vertx);

		// Subscribe to events published for transition
		setRepositoryEvents(new ActualitesRepositoryEvents(config.getBoolean("share-old-groups-to-users", false)));

		addController(new DisplayController());

		// set default rights filter
		setDefaultResourceFilter(new ShareAndOwner());

		// thread table
		SqlConf confThread = SqlConfs.createConf(ThreadController.class.getName());
		confThread.setResourceIdLabel(THREAD_RESOURCE_ID);
		confThread.setTable(THREAD_TABLE);
		confThread.setShareTable(THREAD_SHARE_TABLE);
		confThread.setSchema(getSchema());

		// thread controller
		ThreadController threadController = new ThreadController();
		SqlCrudService threadSqlCrudService = new SqlCrudService(getSchema(), THREAD_TABLE, THREAD_SHARE_TABLE, new JsonArray().addString("*"), new JsonArray().add("*"), true);
		threadController.setCrudService(threadSqlCrudService);
		threadController.setShareService(new SqlShareService(getSchema(),THREAD_SHARE_TABLE, eb, securedActions, null));
		addController(threadController);

		// info table
		SqlConf confInfo = SqlConfs.createConf(InfoController.class.getName());
		confInfo.setResourceIdLabel(INFO_RESOURCE_ID);
		confInfo.setTable(INFO_TABLE);
		confInfo.setShareTable(INFO_SHARE_TABLE);
		confInfo.setSchema(getSchema());

		// info controller
		InfoController infoController = new InfoController();
		SqlCrudService infoSqlCrudService = new SqlCrudService(getSchema(), INFO_TABLE, INFO_SHARE_TABLE, new JsonArray().addString("*"), new JsonArray().add("*"), true);
		infoController.setCrudService(infoSqlCrudService);
		infoController.setShareService(new SqlShareService(getSchema(),INFO_SHARE_TABLE, eb, securedActions, null));
		addController(infoController);

		// comment table
		SqlConf confComment = SqlConfs.createConf(CommentController.class.getName());
		confComment.setTable(COMMENT_TABLE);
		confComment.setSchema(getSchema());

		// comment controller
		CommentController commentController = new CommentController();
		SqlCrudService commentSqlCrudService = new SqlCrudService(getSchema(), COMMENT_TABLE);
		commentController.setCrudService(commentSqlCrudService);
		addController(commentController);

	}

}
