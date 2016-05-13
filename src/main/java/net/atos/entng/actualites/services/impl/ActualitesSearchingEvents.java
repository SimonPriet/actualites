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

package net.atos.entng.actualites.services.impl;

import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.Either.Right;
import org.entcore.common.search.SearchingEvents;
import org.entcore.common.service.SearchService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;

public class ActualitesSearchingEvents implements SearchingEvents {

	private static final Logger log = LoggerFactory.getLogger(ActualitesSearchingEvents.class);
	private SearchService searchService;

	public ActualitesSearchingEvents(SearchService searchService) {
		this.searchService = searchService;
	}

	@Override
	public void searchResource(List<String> appFilters, String userId, JsonArray groupIds, JsonArray searchWords, Integer page, Integer limit, final JsonArray columnsHeader,
							   final String locale, final Handler<Either<String, JsonArray>> handler) {
		if (appFilters.contains(ActualitesSearchingEvents.class.getSimpleName())) {
			final List<String> returnFields = new ArrayList<String>();
			returnFields.add("title");
			returnFields.add("content");
			returnFields.add("owner");
			returnFields.add("modified");
			returnFields.add("thread_id");
			returnFields.add("id");

			final List<String> searchFields = new ArrayList<String>();
			searchFields.add("title");
			searchFields.add("content");
			searchService.search(userId, groupIds.toList(), returnFields, searchWords.toList(), searchFields, page, limit, new Handler<Either<String, JsonArray>>() {
				@Override
				public void handle(Either<String, JsonArray> event) {
					if (event.isRight()) {
						final JsonArray res = formatSearchResult(event.right().getValue(), columnsHeader);
						handler.handle(new Right<String, JsonArray>(res));
					} else {
						handler.handle(new Either.Left<String, JsonArray>(event.left().getValue()));
					}
					if (log.isDebugEnabled()) {
						log.debug("[ActualitesSearchingEvents][searchResource] The resources searched by user are finded");
					}
				}
			});
		} else {
			handler.handle(new Right<String, JsonArray>(new JsonArray()));
		}
	}

	private JsonArray formatSearchResult(final JsonArray results, final JsonArray columnsHeader) {
		final List<String> aHeader = columnsHeader.toList();
		final JsonArray traity = new JsonArray();

		for (int i=0;i<results.size();i++) {
			final JsonObject j = results.get(i);
			final JsonObject jr = new JsonObject();
			if (j != null) {
				jr.putString(aHeader.get(0), j.getString("title"));
				jr.putString(aHeader.get(1), j.getString("content"));
				jr.putObject(aHeader.get(2), new JsonObject().putValue("$date",
						DatatypeConverter.parseDateTime(j.getString("modified")).getTime().getTime()));
				jr.putString(aHeader.get(3), j.getString("username"));
				jr.putString(aHeader.get(4), j.getString("owner"));
				jr.putString(aHeader.get(5), "/actualites#/view/thread/"+
						j.getNumber("thread_id",0) + "/info/"+j.getNumber("id",0));
				traity.add(jr);
			}
		}
		return traity;
	}
}
