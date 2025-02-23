drop view if exists view_prospect_artisan;
drop view if exists view_prospect_actual_status;

create or replace view view_prospect_actual_status as
(
select p.*, actual_status, status_updated_at
from prospect p
         join
     (select ph.id_prospect, ph.status as actual_status, max(ph.updated_at) as status_updated_at
      from prospect_status_history ph
      group by ph.id_prospect, ph.status) ps on p.id = ps.id_prospect);

create or replace view view_prospect_artisan as
(
select p.*, ah.name as artisan_name
from view_prospect_actual_status p
         join account_holder ah on p.id_account_holder = ah.id);