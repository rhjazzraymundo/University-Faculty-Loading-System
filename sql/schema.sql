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
CREATE TABLE schedules (
    schedule_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subject_id  INTEGER     NOT NULL,
    faculty_id  INTEGER,
    room_id     INTEGER     NOT NULL,
    section     VARCHAR(30) NOT NULL,
    term        VARCHAR(30) NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL,
    CONSTRAINT fk_sched_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id),
    CONSTRAINT fk_sched_faculty FOREIGN KEY (faculty_id) REFERENCES faculty (faculty_id),
    CONSTRAINT fk_sched_room    FOREIGN KEY (room_id)    REFERENCES rooms (room_id),
    CONSTRAINT chk_sched_day  CHECK (day_of_week IN
        ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')),
    CONSTRAINT chk_sched_time CHECK (end_time > start_time)
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
    ('DIT',  'Department of Information Technology'),
    ('DCS',  'Department of Computer Science'),
    ('DMATH', 'Department of Mathematics'),
    ('DENG', 'Department of English'),
    ('DPE',  'Department of Physical Education'),
    ('DBA',  'Department of Business Administration'),
    ('DEDU', 'Department of Education'),
    ('DSCI', 'Department of Natural Sciences'),
    ('DSOC', 'Department of Social Sciences'),
    ('DENGR','Department of Engineering');

INSERT INTO faculty (employee_no, first_name, last_name, department_id, email, contact_no, employment_type, max_units, status) VALUES
    ('EMP-001', 'Maria',  'Santos',    1, 'maria.santos@university.edu',  '09171234501', 'Full-time', 24, 'Active'),
    ('EMP-002', 'Jose',   'Reyes',     1, 'jose.reyes@university.edu',    '09171234502', 'Full-time', 24, 'Active'),
    ('EMP-003', 'Ana',    'Cruz',      2, 'ana.cruz@university.edu',      '09171234503', 'Full-time', 24, 'Active'),
    ('EMP-004', 'Pedro',  'Garcia',    2, 'pedro.garcia@university.edu',  '09171234504', 'Part-time', 12, 'Active'),
    ('EMP-005', 'Lia',    'Mendoza',   3, 'lia.mendoza@university.edu',   '09171234505', 'Full-time', 24, 'Active'),
    ('EMP-006', 'Carlos', 'Dela Cruz', 4, 'carlos.delacruz@university.edu','09171234506', 'Full-time', 24, 'Active'),
    ('EMP-007', 'Elena',  'Ramos',     5, 'elena.ramos@university.edu',   '09171234507', 'Part-time', 12, 'Active'),
    ('EMP-008', 'Ramon',  'Aquino',    1, 'ramon.aquino@university.edu',  '09171234508', 'Full-time', 24, 'Inactive'),
    ('EMP-009', 'Grace',  'Villanueva',6, 'grace.villanueva@university.edu','09171234509', 'Full-time', 24, 'Active'),
    ('EMP-010', 'Miguel', 'Torres',    7, 'miguel.torres@university.edu',  '09171234510', 'Full-time', 24, 'Active'),
    ('EMP-011', 'Sofia',  'Bautista',  8, 'sofia.bautista@university.edu', '09171234511', 'Part-time', 12, 'Active'),
    ('EMP-012', 'Daniel', 'Navarro',   9, 'daniel.navarro@university.edu', '09171234512', 'Full-time', 24, 'Active'),
    ('EMP-013', 'Isabel', 'Castillo', 10, 'isabel.castillo@university.edu','09171234513', 'Full-time', 24, 'Active');

INSERT INTO subjects (subject_code, subject_title, units, department_id) VALUES
    ('IT101',   'Introduction to Computing',        3, 1),
    ('IT102',   'Computer Programming 1',           3, 1),
    ('IT201',   'Database Management Systems',      3, 1),
    ('IT202',   'Computer Networking',              3, 1),
    ('CS101',   'Discrete Structures',              3, 2),
    ('CS201',   'Data Structures and Algorithms',   4, 2),
    ('MATH101', 'College Algebra',                  3, 3),
    ('ENG101',  'Purposive Communication',          3, 4),
    ('PE101',   'Physical Fitness',                 2, 5),
    ('IT301',   'Systems Analysis and Design',      3, 1),
    ('BA101',   'Principles of Management',         3, 6),
    ('EDU101',  'The Teaching Profession',          3, 7);

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

-- Sample schedules (no conflicts). Two are left unassigned (faculty_id NULL).
INSERT INTO schedules (subject_id, faculty_id, room_id, section, term, day_of_week, start_time, end_time) VALUES
    (1,  1,    1, 'BSIT-1A', '2026-2027 1st Sem', 'Monday',    '08:00:00', '09:30:00'),
    (2,  1,    5, 'BSIT-1A', '2026-2027 1st Sem', 'Tuesday',   '09:00:00', '12:00:00'),
    (3,  2,    5, 'BSIT-2A', '2026-2027 1st Sem', 'Wednesday', '13:00:00', '16:00:00'),
    (4,  2,    6, 'BSIT-2A', '2026-2027 1st Sem', 'Thursday',  '13:00:00', '16:00:00'),
    (5,  3,    3, 'BSCS-1A', '2026-2027 1st Sem', 'Monday',    '10:00:00', '11:30:00'),
    (6,  3,    6, 'BSCS-2A', '2026-2027 1st Sem', 'Friday',    '08:00:00', '12:00:00'),
    (7,  5,    2, 'BSIT-1A', '2026-2027 1st Sem', 'Wednesday', '10:00:00', '11:30:00'),
    (8,  6,    4, 'BSIT-1A', '2026-2027 1st Sem', 'Thursday',  '08:00:00', '09:30:00'),
    (9,  7,    7, 'BSIT-1A', '2026-2027 1st Sem', 'Saturday',  '08:00:00', '10:00:00'),
    (10, NULL, 1, 'BSIT-3A', '2026-2027 1st Sem', 'Tuesday',   '13:00:00', '14:30:00'),
    (3,  NULL, 6, 'BSIT-2B', '2026-2027 1st Sem', 'Friday',    '13:00:00', '16:00:00');
