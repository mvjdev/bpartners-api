insert into "account_holder"
(id, mobile_phone_number, id_user, email, social_capital, tva_number, initial_cashflow,
 subject_to_vat, verification_status, "name", registration_number, business_activity,
 business_activity_description, address, city, country, postal_code, longitude, latitude, town_code,
 prospecting_perimeter)
values ('b33e6eb0-e262-4596-a91f-20c6a7bfd343', '+33 6 11 22 33 44',
        'joe_doe_id',
        'numer@hei.school', '40000/1', 'FR32123456789', '6000/1', true,
        'VERIFIED', 'NUMER', '899067250', 'businessAndRetail', 'Plombier', '6 RUE PAUL LANGEVIN',
        'FONTENAY-SOUS-BOIS',
        'FRA', '94120', 1.0, 23.5, 92002, 0),
       ('account_holder_id_2', '+33 6 13 22 33 44',
        'jane_doe_id',
        'jane@hei.school', '40000/1', 'FR 31 123456789', '5000/1', true,
        'VERIFIED', 'OTHER', '899067251', 'businessAndRetail', 'Menusier', '6 RUE PAUL LANGEVIN',
        'FONTENAY-SOUS-BOIS',
        'FRA', '94120', null, 23.5, null, 0),
       ('account_holder_id_3', '+33 6 13 22 33 44',
        'bernard_doe_id',
        'bernard@hei.school', '40000/1', 'FR 31 123456789', '5000/1', true,
        'VERIFIED', 'OTHER', '899067251', 'businessAndRetail', 'Menusier', '6 RUE PAUL LANGEVIN',
        'FONTENAY-SOUS-BOIS',
        'FRA', '94120', null, 23.5, null, 0);