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
