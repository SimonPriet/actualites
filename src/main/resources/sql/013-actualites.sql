CREATE INDEX info_share_idx ON actualites.info_shares USING btree (resource_id);
CREATE INDEX thread_share_idx ON actualites.thread_shares USING btree (resource_id);
CREATE INDEX info_thread_id_idx ON actualites.info USING btree (thread_id);
CREATE INDEX info_status_idx ON actualites.info USING btree (status);
CREATE INDEX members_group_id_idx ON actualites.members USING btree (group_id);