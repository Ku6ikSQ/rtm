INSERT INTO users (id, username, email, password_hash, role, created_at, is_active, image_key)
VALUES (
           gen_random_uuid(),
           'admin_user',
           'admin@example.com',
           '$2a$12$OBjC9N7PkA6i/Dwe1tpzbO.wRD.MNbSG5RmB5xZ0spZG12yECnOfy',
           'ADMIN',
           now(),
           true,
           NULL
       );