insert into "invoice"
(id, id_account, title, "ref", sending_date, to_pay_at, status)
values ('invoice1_id', 'beed1765-5c16-472a-b3f4-5c376ce5db58', 'Facture tableau',
        'BP001',
        '2022-09-01',
        '2022-10-01', 'CONFIRMED'),
       ('invoice2_id', 'beed1765-5c16-472a-b3f4-5c376ce5db58', 'Facture ' ||
                                                               'plomberie', 'BP002',
        '2022-09-10',
        '2022-10-10', 'CONFIRMED');