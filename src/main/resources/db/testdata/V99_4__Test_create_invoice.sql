insert into "invoice"
(id, id_user, title, "ref", id_customer, sending_date, validity_date, to_pay_at, status, comment,
 "created_datetime", payment_url, file_id, payment_type)
values ('invoice1_id', 'joe_doe_id', 'Outils pour plomberie',
        'BP001', 'customer1_id',
        '2022-09-01',
        '2022-10-03', '2022-10-01', 'CONFIRMED', null, '2022-01-01T01:00:00.00Z'
           , 'https://connect-v2-sbx.fintecture.com', 'file1_id', 'IN_INSTALMENT'),
       ('invoice2_id', 'joe_doe_id', 'Facture ' ||
                                     'plomberie', 'BP002',
        'customer2_id',
        '2022-09-10',
        '2022-10-14', '2022-10-10', 'CONFIRMED', null, '2022-01-01T03:00:00.00Z',
        'https://connect-v2-sbx.fintecture.com', null, 'CASH');
insert into "invoice"
(id, id_user, title, "ref", id_customer, sending_date, validity_date, to_pay_at, status, comment,
 "created_datetime", file_id)
values ('invoice3_id', 'joe_doe_id', 'Facture ' ||
                                     'transaction ', 'BP004',
        'customer1_id',
        '2022-10-12',
        '2022-10-03', '2022-11-10', 'DRAFT', null, '2022-01-01T02:00:00.00Z', 'invoice3_file_id'),
       ('invoice4_id', 'joe_doe_id', 'Facture ' ||
                                     'achat', 'BP005',
        'customer1_id',
        '2022-10-12',
        '2022-10-03', '2022-11-13', 'PROPOSAL', null, '2022-01-01T04:00:00.00Z', 'invoice4_file_id'),
       ('invoice5_id', 'joe_doe_id', 'Facture ' ||
                                     'achat', 'BP006',
        'customer1_id',
        '2022-10-12',
        '2022-10-03', '2022-11-13', 'PROPOSAL', null, '2022-01-01T05:00:00.00Z', 'invoice5_file_id'),
       ('invoice6_id', 'joe_doe_id', 'Facture ' ||
                                     'transaction', 'BP007',
        'customer1_id',
        '2022-10-12',
        '2022-11-12', '2022-11-10', 'DRAFT', null, '2022-01-01T06:00:00.00Z', 'invoice6_file_id'),
       ('invoice7_id', 'joe_doe_id', 'Facture ' ||
                                     'transaction', 'BP008',
        'customer1_id',
        '2022-10-12',
        '2022-10-03', '2022-11-10', 'PAID', null, '2022-01-01T07:00:00.00Z', 'invoice7_file_id');

insert into "invoice"
(id, id_user, title, "ref", id_customer, sending_date, validity_date, to_pay_at, status, comment,
 "created_datetime", archive_status, file_id)
values ('invoice8_id', 'joe_doe_id', 'Devis ' ||
                                     'transaction ', 'BP009',
        'customer1_id',
        '2022-10-12',
        '2022-10-03', '2022-11-10', 'PROPOSAL', null, '2023-04-05T02:00:00.00Z', 'DISABLED', 'invoice8_file_id');