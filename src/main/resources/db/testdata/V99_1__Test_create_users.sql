insert into "user"
(id, swan_user_id, phone_number, email, monthly_subscription, status, logo_file_id, first_name,
 last_name, identification_status, id_verified)
values ('joe_doe_id', 'c15924bf-61f9-4381-8c9b-d34369bf91f7', '+261340465338', 'joe@email.com', 5,
        'ENABLED',
        'logo.jpeg', 'Joe', 'Doe', 'VALID_IDENTITY', true),
       ('jane_doe_id', 'jane_doe_user_id', '+261341122334', 'jane@email.com', 5, 'ENABLED',
        'logo.jpeg', null, null, null, null);