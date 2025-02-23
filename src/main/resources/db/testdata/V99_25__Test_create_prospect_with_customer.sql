insert into prospect (id, old_name, old_phone, old_email, old_address, id_account_holder, town_code, rating,
                      last_evaluation_date, first_name)
values ('prospect1_id', 'John doe', null, null, null,
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', 92002, 9.993, '2023-01-01T00:00:00.00Z', 'john'),
       ('prospect2_id', 'jane doe', '+261340465339', 'janeDoe@gmail.com', '30 Rue de la Montagne Sainte-Genevieve',
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', 92002, -1, null, null),
       ('prospect3_id', 'markus adams', '+261340465340', 'markusAdams@gmail.com',
        '30 Rue de la Montagne Sainte-Genevieve',
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', 92001, 0, '2023-01-01T00:00:00.00Z', null),
       ('prospect4_id', 'Alyssa Hain', '+261340465341', 'alyssaHain@gmail.com',
        '30 Rue de la Montagne Sainte-Genevieve',
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', null, -1, null, null),
       ('prospect5_id', 'Michele Klaffs', '+261340465342', 'micheleKlaffs@gmail.com',
        '30 Rue de la Montagne Sainte-Genevieve',
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', null, -1, null, null),
       ('prospect6_id', 'Timmie	Accombe', '+261340465343', 'timmieAccombe@gmail.com',
        '30 Rue de la Montagne Sainte-Genevieve',
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', null, -1, null, null),
       ('prospect7_id', 'Killy	Waddilove', '+261340465344', 'killyWaddilove@gmail.com',
        '30 Rue de la Montagne Sainte-Genevieve',
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', null, -1, null, null);
insert into prospect (id, old_name, old_phone, old_email, old_address, id_account_holder, town_code, rating,
                      last_evaluation_date, pos_latitude, pos_longitude, first_name)
values ('prospect8_id', 'Johnny	Paul', '+261340465345', 'johnny@gmail.com',
        '30 Rue de la Montagne Sainte-Genevieve',
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', null, -1, null, 1.0, 1.0, 'paul'),
       ('prospect9_id', 'Johnny	Pauline', '+261340465346', 'johnnyp@gmail.com',
        '30 Rue de la Montagne Sainte-Genevieve',
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', null, -1, null, 1.0, 1.0, 'pauline'),
       ('prospect10_id', 'Johnny	Paulinette', '+261340465347', 'johnnypette@gmail.com',
        '30 Rue de la Montagne Sainte-Genevieve',
        'b33e6eb0-e262-4596-a91f-20c6a7bfd343', null, -1, null, null, null, 'paulinette');

insert into "prospect_status_history"(id, id_prospect, status, updated_at)
values ('prospect_status1_id', 'prospect1_id', 'TO_CONTACT', '2023-01-01T00:00:00.00Z'),
       ('prospect_status2_id', 'prospect2_id', 'TO_CONTACT', '2023-01-01T00:00:00.00Z'),
       ('prospect_status3_id', 'prospect3_id', 'TO_CONTACT', '2023-01-01T00:00:00.00Z'),
       ('prospect_status4_id', 'prospect4_id', 'CONTACTED', '2023-01-01T00:00:00.00Z'),
       ('prospect_status5_id', 'prospect5_id', 'CONTACTED', '2023-01-01T00:00:00.00Z'),
       ('prospect_status6_id', 'prospect6_id', 'CONVERTED', '2023-01-01T00:00:00.00Z'),
       ('prospect_status7_id', 'prospect7_id', 'CONVERTED', '2023-01-01T00:00:00.00Z'),
       ('prospect_status8_id', 'prospect8_id', 'TO_CONTACT', '2023-01-01T00:00:00.00Z'),
       ('prospect_status9_id', 'prospect9_id', 'TO_CONTACT', '2023-01-01T00:00:00.00Z'),
       ('prospect_status10_id', 'prospect10_id', 'TO_CONTACT', '2023-01-01T00:00:00.00Z');

insert into "customer"
(id, id_user, first_name, last_name, email, phone, website, address, zip_code, city, country,
 comment, latitude, longitude, customer_type, is_converted)
values ('prospect_8_customer_1_id', 'joe_doe_id', 'Johnny Paul', '', 'johnny@gmail.com',
        '+261340465345', 'https://johnny.website.com', '30 Rue de la Montagne Sainte-Genevieve', 95160,
        'Metz', null, 'Rencontre avec Johnny', 0, 0, 'INDIVIDUAL', false);

insert into "has_customer" (id, id_prospect, id_customer)
VALUES ('has_customer_prospect_8_customer_1_id', 'prospect8_id', 'prospect_8_customer_1_id')

