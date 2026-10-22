-- ============================================================
-- University Faculty Loading System : Apache Derby schema + sample data
-- Run with database.DatabaseSetup (Right-click > Run File), or in any SQL tool.
-- Running it again resets the database to the sample data below.
-- ============================================================

-- ---------- Drop old tables (child tables first) ----------
DROP TABLE schedules;
DROP TABLE subjects;
DROP TABLE faculty;
DROP TABLE rooms;
DROP TABLE departments;
DROP TABLE users;

-- ---------- USERS ----------
CREATE TABLE users (
    user_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role      VARCHAR(30)  NOT NULL DEFAULT 'Staff',
    status    VARCHAR(20)  NOT NULL DEFAULT 'Active',
    CONSTRAINT chk_users_role   CHECK (role IN ('Admin', 'Staff')),
    CONSTRAINT chk_users_status CHECK (status IN ('Active', 'Inactive'))
);

-- ---------- DEPARTMENTS ----------
CREATE TABLE departments (
    department_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    department_code VARCHAR(20)  NOT NULL UNIQUE,
    department_name VARCHAR(100) NOT NULL UNIQUE
);

-- ---------- FACULTY ----------
CREATE TABLE faculty (
    faculty_id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_no     VARCHAR(20)  NOT NULL UNIQUE,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    department_id   INTEGER      NOT NULL,
    email           VARCHAR(100),
    contact_no      VARCHAR(30),
    employment_type VARCHAR(20)  NOT NULL DEFAULT 'Full-time',
    max_units       INTEGER      NOT NULL DEFAULT 24,
    status          VARCHAR(20)  NOT NULL DEFAULT 'Active',
    CONSTRAINT fk_faculty_department FOREIGN KEY (department_id)
        REFERENCES departments (department_id),
    CONSTRAINT chk_faculty_type   CHECK (employment_type IN ('Full-time', 'Part-time')),
    CONSTRAINT chk_faculty_units  CHECK (max_units > 0),
    CONSTRAINT chk_faculty_status CHECK (status IN ('Active', 'Inactive'))
);

-- ---------- SUBJECTS ----------
CREATE TABLE subjects (
    subject_id    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subject_code  VARCHAR(20)  NOT NULL UNIQUE,
    subject_title VARCHAR(100) NOT NULL,
    units         INTEGER      NOT NULL,
    department_id INTEGER      NOT NULL,
    CONSTRAINT fk_subjects_department FOREIGN KEY (department_id)
        REFERENCES departments (department_id),
    CONSTRAINT chk_subjects_units CHECK (units BETWEEN 1 AND 6)
);

-- ---------- ROOMS ----------
CREATE TABLE rooms (
    room_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_name VARCHAR(30) NOT NULL UNIQUE,
    building  VARCHAR(50),
    capacity  INTEGER     NOT NULL DEFAULT 40,
    room_type VARCHAR(20) NOT NULL DEFAULT 'Lecture',
    CONSTRAINT chk_rooms_capacity CHECK (capacity > 0),
    CONSTRAINT chk_rooms_type     CHECK (room_type IN ('Lecture', 'Laboratory'))
);

-- ---------- SCHEDULES (faculty_id stays NULL until a faculty is assigned) ----------
-- session_type separates lecture from laboratory hours (lab hours are credited at 0.75).
-- Classes must fall within working hours: 7:00 AM to 6:00 PM.
CREATE TABLE schedules (
    schedule_id  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subject_id   INTEGER     NOT NULL,
    faculty_id   INTEGER,
    room_id      INTEGER     NOT NULL,
    section      VARCHAR(30) NOT NULL,
    term         VARCHAR(30) NOT NULL,
    day_of_week  VARCHAR(10) NOT NULL,
    start_time   TIME        NOT NULL,
    end_time     TIME        NOT NULL,
    session_type VARCHAR(12) NOT NULL DEFAULT 'Lecture',
    CONSTRAINT fk_sched_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id),
    CONSTRAINT fk_sched_faculty FOREIGN KEY (faculty_id) REFERENCES faculty (faculty_id),
    CONSTRAINT fk_sched_room    FOREIGN KEY (room_id)    REFERENCES rooms (room_id),
    CONSTRAINT chk_sched_day  CHECK (day_of_week IN
        ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')),
    CONSTRAINT chk_sched_time CHECK (end_time > start_time),
    CONSTRAINT chk_sched_hours CHECK (start_time >= TIME('07:00:00') AND end_time <= TIME('18:00:00')),
    CONSTRAINT chk_sched_session CHECK (session_type IN ('Lecture', 'Laboratory'))
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Login accounts:  admin / admin123   and   staff / staff123
INSERT INTO users (username, password, full_name, role, status) VALUES
    ('admin', 'admin123', 'System Administrator', 'Admin', 'Active'),
    ('staff', 'staff123', 'Faculty Loading Staff', 'Staff', 'Active'),
    ('olduser', 'old123', 'Inactive Account', 'Staff', 'Inactive');

INSERT INTO departments (department_code, department_name) VALUES
    ('COE', 'College of Engineering'),
    ('CET', 'College of Engineering Technology'),
    ('CES', 'College of Education and Science');

INSERT INTO faculty (employee_no, first_name, last_name, department_id, email, contact_no, employment_type, max_units, status) VALUES
    ('EMP-001', 'Maria',  'Santos',     2, 'maria.santos@university.edu',  '09171234501', 'Full-time', 24, 'Active'),
    ('EMP-002', 'Jose',   'Reyes',      2, 'jose.reyes@university.edu',    '09171234502', 'Full-time', 24, 'Active'),
    ('EMP-003', 'Ana',    'Cruz',       2, 'ana.cruz@university.edu',      '09171234503', 'Full-time', 24, 'Active'),
    ('EMP-004', 'Pedro',  'Garcia',     2, 'pedro.garcia@university.edu',  '09171234504', 'Part-time', 12, 'Active'),
    ('EMP-005', 'Lia',    'Mendoza',    3, 'lia.mendoza@university.edu',   '09171234505', 'Full-time', 24, 'Active'),
    ('EMP-006', 'Carlos', 'Dela Cruz',  3, 'carlos.delacruz@university.edu','09171234506', 'Full-time', 24, 'Active'),
    ('EMP-007', 'Elena',  'Ramos',      3, 'elena.ramos@university.edu',   '09171234507', 'Part-time', 12, 'Active'),
    ('EMP-008', 'Ramon',  'Aquino',     2, 'ramon.aquino@university.edu',  '09171234508', 'Full-time', 24, 'Inactive'),
    ('EMP-009', 'Grace',  'Villanueva', 3, 'grace.villanueva@university.edu','09171234509', 'Full-time', 24, 'Active'),
    ('EMP-010', 'Miguel', 'Torres',     3, 'miguel.torres@university.edu',  '09171234510', 'Full-time', 24, 'Active'),
    ('EMP-011', 'Sofia',  'Bautista',   3, 'sofia.bautista@university.edu', '09171234511', 'Part-time', 12, 'Active'),
    ('EMP-012', 'Daniel', 'Navarro',    3, 'daniel.navarro@university.edu', '09171234512', 'Full-time', 24, 'Active'),
    ('EMP-013', 'Isabel', 'Castillo',  1, 'isabel.castillo@university.edu','09171234513', 'Full-time', 24, 'Active');

INSERT INTO subjects (subject_code, subject_title, units, department_id) VALUES
    ('IT101',   'Introduction to Computing',        3, 2),
    ('IT102',   'Computer Programming 1',           3, 2),
    ('IT201',   'Database Management Systems',      3, 2),
    ('IT202',   'Computer Networking',              3, 2),
    ('CS101',   'Discrete Structures',              3, 2),
    ('CS201',   'Data Structures and Algorithms',   4, 2),
    ('MATH101', 'College Algebra',                  3, 3),
    ('ENG101',  'Purposive Communication',          3, 3),
    ('PE101',   'Physical Fitness',                 2, 3),
    ('IT301',   'Systems Analysis and Design',      3, 2),
    ('BA101',   'Principles of Management',         3, 3),
    ('EDU101',  'The Teaching Profession',          3, 3);

INSERT INTO rooms (room_name, building, capacity, room_type) VALUES
    ('Room 101', 'Main Building',  40, 'Lecture'),
    ('Room 102', 'Main Building',  40, 'Lecture'),
    ('Room 201', 'Main Building',  45, 'Lecture'),
    ('Room 202', 'Main Building',  45, 'Lecture'),
    ('Lab 1',    'IT Building',    30, 'Laboratory'),
    ('Lab 2',    'IT Building',    30, 'Laboratory'),
    ('Gym',      'Sports Complex', 60, 'Lecture'),
    ('Room 301', 'Main Building',  45, 'Lecture'),
    ('Room 302', 'Main Building',  45, 'Lecture'),
    ('Lab 3',    'IT Building',    30, 'Laboratory');

-- Sample schedules (no conflicts, all within 7:00 AM - 6:00 PM). Two are left unassigned (faculty_id NULL).
-- Laboratory sessions are the ones held in Lab 1, Lab 2 and Lab 3.
INSERT INTO schedules (subject_id, faculty_id, room_id, section, term, day_of_week, start_time, end_time, session_type) VALUES
    (1,  1,    1, 'BSIT-1A', '2026-2027 1st Sem', 'Monday',    '08:00:00', '09:30:00', 'Lecture'),
    (2,  1,    5, 'BSIT-1A', '2026-2027 1st Sem', 'Tuesday',   '09:00:00', '12:00:00', 'Laboratory'),
    (3,  2,    5, 'BSIT-2A', '2026-2027 1st Sem', 'Wednesday', '13:00:00', '16:00:00', 'Laboratory'),
    (4,  2,    6, 'BSIT-2A', '2026-2027 1st Sem', 'Thursday',  '13:00:00', '16:00:00', 'Laboratory'),
    (5,  3,    3, 'BSCS-1A', '2026-2027 1st Sem', 'Monday',    '10:00:00', '11:30:00', 'Lecture'),
    (6,  3,    6, 'BSCS-2A', '2026-2027 1st Sem', 'Friday',    '08:00:00', '12:00:00', 'Laboratory'),
    (7,  5,    2, 'BSIT-1A', '2026-2027 1st Sem', 'Wednesday', '10:00:00', '11:30:00', 'Lecture'),
    (8,  6,    4, 'BSIT-1A', '2026-2027 1st Sem', 'Thursday',  '08:00:00', '09:30:00', 'Lecture'),
    (9,  7,    7, 'BSIT-1A', '2026-2027 1st Sem', 'Saturday',  '08:00:00', '10:00:00', 'Lecture'),
    (10, NULL, 1, 'BSIT-3A', '2026-2027 1st Sem', 'Tuesday',   '13:00:00', '14:30:00', 'Lecture'),
    (3,  NULL, 6, 'BSIT-2B', '2026-2027 1st Sem', 'Friday',    '13:00:00', '16:00:00', 'Laboratory'),
    (5,  4,    8, 'BSCS-1B', '2026-2027 1st Sem', 'Monday',    '08:00:00', '09:30:00', 'Lecture'),
    (4,  4,   10, 'BSIT-2B', '2026-2027 1st Sem', 'Tuesday',   '14:00:00', '15:30:00', 'Laboratory'),
    (2,  4,    8, 'BSIT-1B', '2026-2027 1st Sem', 'Wednesday', '08:00:00', '09:30:00', 'Lecture'),
    (8,  4,    8, 'BSIT-1B', '2026-2027 1st Sem', 'Friday',    '08:00:00', '09:30:00', 'Lecture');

-- Sample overtime case: Miguel Torres (faculty 10) has 20 lecture hours = 18 regular + 2 overtime.
INSERT INTO schedules (subject_id, faculty_id, room_id, section, term, day_of_week, start_time, end_time, session_type) VALUES
    (11, 10, 9, 'BSED-2A', '2026-2027 1st Sem', 'Monday',    '08:00:00', '12:00:00', 'Lecture'),
    (12, 10, 9, 'BSED-2A', '2026-2027 1st Sem', 'Tuesday',   '08:00:00', '12:00:00', 'Lecture'),
    (7,  10, 9, 'BSED-2A', '2026-2027 1st Sem', 'Wednesday', '08:00:00', '12:00:00', 'Lecture'),
    (8,  10, 9, 'BSED-2A', '2026-2027 1st Sem', 'Thursday',  '08:00:00', '12:00:00', 'Lecture'),
    (11, 10, 9, 'BSED-2A', '2026-2027 1st Sem', 'Friday',    '08:00:00', '12:00:00', 'Lecture');

-- Sample lab-weight case: Daniel Navarro (faculty 12) has 12 lecture + 8 lab hours = 12 + 8 x 0.75 = 18 (full load).
INSERT INTO schedules (subject_id, faculty_id, room_id, section, term, day_of_week, start_time, end_time, session_type) VALUES
    (3,  12, 8,  'BSIT-3B', '2026-2027 1st Sem', 'Monday',    '13:00:00', '17:00:00', 'Lecture'),
    (4,  12, 8,  'BSIT-3B', '2026-2027 1st Sem', 'Tuesday',   '13:00:00', '17:00:00', 'Lecture'),
    (10, 12, 8,  'BSIT-3B', '2026-2027 1st Sem', 'Wednesday', '13:00:00', '17:00:00', 'Lecture'),
    (2,  12, 10, 'BSIT-3B', '2026-2027 1st Sem', 'Thursday',  '08:00:00', '12:00:00', 'Laboratory'),
    (4,  12, 10, 'BSIT-3B', '2026-2027 1st Sem', 'Friday',    '13:00:00', '17:00:00', 'Laboratory');


-- Load for the remaining active faculty, so every active faculty member has a load in the sample data.
-- Sofia Bautista (faculty 11): 9 lecture + 3 lab hrs = 9 + 3 x 0.75 = 11.25
-- Grace Villanueva (faculty 9): 15 lecture hrs
-- Isabel Castillo (faculty 13): 12 lecture + 3 lab hrs = 12 + 3 x 0.75 = 14.25
INSERT INTO schedules (subject_id, faculty_id, room_id, section, term, day_of_week, start_time, end_time, session_type) VALUES
    (11, 11, 4, 'BSED-1A', '2026-2027 1st Sem', 'Monday',    '13:00:00', '16:00:00', 'Lecture'),
    (12, 11, 4, 'BSED-1A', '2026-2027 1st Sem', 'Tuesday',   '13:00:00', '16:00:00', 'Lecture'),
    (7,  11, 4, 'BSED-1A', '2026-2027 1st Sem', 'Wednesday', '13:00:00', '16:00:00', 'Lecture'),
    (2,  11, 5, 'BSED-1A', '2026-2027 1st Sem', 'Thursday',  '08:00:00', '11:00:00', 'Laboratory'),

    (8,  9,  3, 'BSED-1B', '2026-2027 1st Sem', 'Monday',    '13:00:00', '16:00:00', 'Lecture'),
    (7,  9,  3, 'BSED-1B', '2026-2027 1st Sem', 'Tuesday',   '08:00:00', '11:00:00', 'Lecture'),
    (12, 9,  3, 'BSED-1B', '2026-2027 1st Sem', 'Wednesday', '08:00:00', '11:00:00', 'Lecture'),
    (11, 9,  3, 'BSED-1B', '2026-2027 1st Sem', 'Thursday',  '08:00:00', '11:00:00', 'Lecture'),
    (9,  9,  3, 'BSED-1B', '2026-2027 1st Sem', 'Friday',    '08:00:00', '11:00:00', 'Lecture'),

    (1,  13, 2, 'BSIT-4A', '2026-2027 1st Sem', 'Monday',    '08:00:00', '11:00:00', 'Lecture'),
    (5,  13, 2, 'BSIT-4A', '2026-2027 1st Sem', 'Tuesday',   '08:00:00', '11:00:00', 'Lecture'),
    (6,  13, 2, 'BSIT-4A', '2026-2027 1st Sem', 'Thursday',  '08:00:00', '11:00:00', 'Lecture'),
    (10, 13, 2, 'BSIT-4A', '2026-2027 1st Sem', 'Friday',    '08:00:00', '11:00:00', 'Lecture'),
    (3,  13, 6, 'BSIT-4A', '2026-2027 1st Sem', 'Tuesday',   '13:00:00', '16:00:00', 'Laboratory');


-- One more faculty with overtime: Patricia Lim (faculty 14), CET.
-- 15 lecture + 8 lab hrs = 15 + 8 x 0.75 = 21 credited hrs = 18 regular (FTE) + 3 overtime.
INSERT INTO faculty (employee_no, first_name, last_name, department_id, email, contact_no, employment_type, max_units, status) VALUES
    ('EMP-014', 'Patricia', 'Lim', 2, 'patricia.lim@university.edu', '09171234514', 'Full-time', 24, 'Active');

INSERT INTO schedules (subject_id, faculty_id, room_id, section, term, day_of_week, start_time, end_time, session_type) VALUES
    (1,  14, 9, 'BSIT-4B', '2026-2027 1st Sem', 'Monday',    '13:00:00', '16:00:00', 'Lecture'),
    (5,  14, 9, 'BSIT-4B', '2026-2027 1st Sem', 'Tuesday',   '13:00:00', '16:00:00', 'Lecture'),
    (6,  14, 9, 'BSIT-4B', '2026-2027 1st Sem', 'Wednesday', '13:00:00', '16:00:00', 'Lecture'),
    (4,  14, 9, 'BSIT-4B', '2026-2027 1st Sem', 'Thursday',  '13:00:00', '16:00:00', 'Lecture'),
    (10, 14, 9, 'BSIT-4B', '2026-2027 1st Sem', 'Friday',    '13:00:00', '16:00:00', 'Lecture'),
    (2,  14, 5, 'BSIT-4B', '2026-2027 1st Sem', 'Monday',    '08:00:00', '12:00:00', 'Laboratory'),
    (3,  14, 5, 'BSIT-4B', '2026-2027 1st Sem', 'Friday',    '08:00:00', '12:00:00', 'Laboratory');