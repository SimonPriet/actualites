package net.atos.entng.actualites.filters;

import static org.entcore.common.sql.Sql.parseId;

import java.util.ArrayList;
import java.util.List;

import net.atos.entng.actualites.controllers.InfoController;

import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlConf;
import org.entcore.common.sql.SqlConfs;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.http.Binding;

public class InfoFilter implements ResourcesProvider {

	@Override
	public void authorize(final HttpServerRequest request, final Binding binding, final UserInfos user, final Handler<Boolean> handler) {
		SqlConf conf = SqlConfs.getConf(InfoController.class.getName());
		String id = null;
		if(isInfoShare(binding)){
			id = request.params().get("id");
		} else {
			id = request.params().get(conf.getResourceIdLabel());
		}
		if (id != null && !id.trim().isEmpty() && (parseId(id) instanceof Integer)) {
			request.pause();
			// Method
			String sharedMethod = binding.getServiceMethod().replaceAll("\\.", "-");

			// Groups and users
			final List<String> groupsAndUserIds = new ArrayList<>();
			groupsAndUserIds.add(user.getUserId());
			if (user.getProfilGroupsIds() != null) {
				groupsAndUserIds.addAll(user.getProfilGroupsIds());
			}
			// Query
			StringBuilder query = new StringBuilder();
			JsonArray values = new JsonArray();
			query.append("SELECT count(*)")
				.append(" FROM actualites.info AS i")
				.append(" LEFT JOIN actualites.info_shares AS ios ON i.id = ios.resource_id")
				.append(" LEFT JOIN actualites.thread AS t ON i.thread_id = t.id")
				.append(" LEFT JOIN actualites.thread_shares AS ts ON t.id = ts.resource_id")
				.append(" WHERE i.id = ? ")
				.append(" AND ((i.owner = ? OR (ios.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " AND ios.action = ? AND i.status > 2))")
				.append(" OR (((t.owner = ? OR ((ts.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + ") AND ts.action = ?)) AND i.status > 1))");
			values.add(Sql.parseId(id));
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(sharedMethod);
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			if(isInfoAction(binding) || isInfoPending(binding)){
				values.add("net-atos-entng-actualites-controllers-InfoController|publish");
			} else {
				values.add(sharedMethod);
			}
			// Execute
			Sql.getInstance().prepared(query.toString(), values, new Handler<Message<JsonObject>>() {
				@Override
				public void handle(Message<JsonObject> message) {
					request.resume();
					Long count = SqlResult.countResult(message);
					handler.handle(count != null && count > 0);
				}
			});
		} else {
			handler.handle(false);
		}
	}

	private boolean isInfoAction(final Binding binding) {
		return ("net.atos.entng.actualites.controllers.InfoController|getInfo".equals(binding.getServiceMethod()) ||
				 "net.atos.entng.actualites.controllers.CommentController|Comment".equals(binding.getServiceMethod()) ||
				 "net.atos.entng.actualites.controllers.CommentController|updateComment".equals(binding.getServiceMethod()) ||
				 "net.atos.entng.actualites.controllers.CommentController|deleteComment".equals(binding.getServiceMethod() )
				);
	}

	private boolean isInfoShare(final Binding binding) {
		return ("net.atos.entng.actualites.controllers.InfoController|shareInfo".equals(binding.getServiceMethod()) ||
				 "net.atos.entng.actualites.controllers.InfoController|shareInfoSubmit".equals(binding.getServiceMethod()) ||
				 "net.atos.entng.actualites.controllers.InfoController|shareInfoRemove".equals(binding.getServiceMethod() )
				);
	}

	private boolean isInfoPending(final Binding binding) {
		return ("net.atos.entng.actualites.controllers.InfoController|unsubmit".equals(binding.getServiceMethod()) ||
				"net.atos.entng.actualites.controllers.InfoController|updatePending".equals(binding.getServiceMethod()));
	}
}
