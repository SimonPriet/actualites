/*
 * Copyright © Région Nord Pas de Calais-Picardie,  Département 91, Région Aquitaine-Limousin-Poitou-Charentes, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package net.atos.entng.actualites.filters;

import fr.wseduc.webutils.http.Binding;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.entcore.common.utils.StringUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class CommentFilter extends InfoFilter  {

    private static final Logger log = LoggerFactory.getLogger(CommentFilter.class);

	@Override
	public void authorize(final HttpServerRequest request, final Binding binding, final UserInfos user, final Handler<Boolean> handler) {

		super.authorize(request, binding, user, new Handler<Boolean>() {
			@Override
			public void handle(Boolean event) {

                if(event.booleanValue()){
                    // The owner of the news has the right to delete the comment
                    // Or Users who has the right to publish a news on the thread has the right to delete the comment
                    handler.handle(true);
                } else {
                    request.pause();

                    String id = request.params().get("id");
                    if(StringUtils.isEmpty(id)) {
                        log.error("id comment is null or emply for delete comment infoid : " + request.params().get("infoid"));
                        handler.handle(false);
                    } else {
                        // The owner of the comment has the right to delete the comment
                        StringBuilder query = new StringBuilder();
                        query.append("SELECT count(*)")
                                .append(" FROM actualites.comment AS c")
                                .append(" WHERE c.owner = ? ")
                                .append(" AND c.id = ? ");
                        JsonArray values = new JsonArray().add(user.getUserId()).add(Sql.parseId(id));

                        // Execute
                        Sql.getInstance().prepared(query.toString(), values, new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> message) {
                                request.resume();
                                Long count = SqlResult.countResult(message);
                                handler.handle(count != null && count > 0);
                            }
                        });
                    }
                }
			}
		});


	}
}
