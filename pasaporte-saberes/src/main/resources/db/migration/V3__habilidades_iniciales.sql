INSERT INTO habilidad (nombre, categoria) VALUES
    ('Guitarra', 'Arte y cultura'),
    ('Fotografía', 'Arte y cultura'),
    ('Inglés', 'Idiomas'),
    ('Cocina', 'Bienestar'),
    ('Cerámica', 'Arte y cultura'),
    ('Programación', 'Tecnología'),
    ('Matemáticas', 'Educación'),
    ('Costura', 'Oficios'),
    ('Reparaciones básicas', 'Oficios'),
    ('Yoga', 'Bienestar')
ON CONFLICT (nombre) DO NOTHING;
