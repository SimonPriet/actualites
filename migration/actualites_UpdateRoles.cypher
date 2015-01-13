begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|view'
, 'net.atos.entng.actualites.controllers.DisplayController|view');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|getThread'
, 'net.atos.entng.actualites.controllers.ThreadController|getThread');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|listThreadInfos'
, 'net.atos.entng.actualites.controllers.ThreadController|listInfosByThreadId');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|updateThread'
, 'net.atos.entng.actualites.controllers.ThreadController|updateThread');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|deleteThread'
, 'net.atos.entng.actualites.controllers.ThreadController|deleteThread');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|shareThread'
, 'net.atos.entng.actualites.controllers.ThreadController|shareThread');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|shareThreadRemove'
, 'net.atos.entng.actualites.controllers.ThreadController|shareThreadRemove');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|shareThreadSubmit'
, 'net.atos.entng.actualites.controllers.ThreadController|shareThreadSubmit');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|getInfo'
, 'net.atos.entng.actualites.controllers.InfoController|getInfo');
commit

begin transaction
match (a:Action {name: 'net.atos.entng.actualites.controllers.InfoController|getInfo'}) 
SET a.displayName = replace(a.displayName, 'thread.contrib', 'info.read');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|createDraft'
, 'net.atos.entng.actualites.controllers.InfoController|createDraft');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|updateDraft'
, 'net.atos.entng.actualites.controllers.InfoController|updateDraft');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|updatePending'
, 'net.atos.entng.actualites.controllers.InfoController|updatePending');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|updatePublished'
, 'net.atos.entng.actualites.controllers.InfoController|updatePublished');
commit

begin transaction
match (a:Action {name: 'net.atos.entng.actualites.controllers.InfoController|updatePublished'}) 
SET a.displayName = replace(a.displayName, 'thread.manage', 'thread.publish');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|publish'
, 'net.atos.entng.actualites.controllers.InfoController|publish');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|submit'
, 'net.atos.entng.actualites.controllers.InfoController|submit');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|unpublish'
, 'net.atos.entng.actualites.controllers.InfoController|unpublish');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|unsubmit'
, 'net.atos.entng.actualites.controllers.InfoController|unsubmit');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|trash'
, 'net.atos.entng.actualites.controllers.InfoController|trash');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|restore'
, 'net.atos.entng.actualites.controllers.InfoController|restore');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|delete'
, 'net.atos.entng.actualites.controllers.InfoController|delete');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|comment'
, 'net.atos.entng.actualites.controllers.CommentController|comment');
commit

begin transaction
match (a:Action {name: 'net.atos.entng.actualites.controllers.CommentController|comment'}) 
SET a.displayName = replace(a.displayName, 'thread.comment', 'info.comment');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|deleteComment'
, 'net.atos.entng.actualites.controllers.CommentController|deleteComment');
commit

begin transaction
match (a:Action {name: 'net.atos.entng.actualites.controllers.CommentController|comment'}) 
SET a.displayName = replace(a.displayName, 'thread.comment', 'info.comment');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|listThreads'
, 'net.atos.entng.actualites.controllers.ThreadController|listThreads');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|createThread'
, 'net.atos.entng.actualites.controllers.ThreadController|createThread');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|listInfos'
, 'net.atos.entng.actualites.controllers.InfoController|listInfos');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|listLastPublishedInfos'
, 'net.atos.entng.actualites.controllers.InfoController|listLastPublishedInfos');
commit

begin transaction
match (a:Action) SET a.name = replace(a.name, 'net.atos.entng.actualites.controllers.ActualitesController|listInfosForLinker'
, 'net.atos.entng.actualites.controllers.InfoController|listInfosForLinker');
commit
