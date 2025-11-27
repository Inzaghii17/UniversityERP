

create table courses
(
    course_id int auto_increment
        primary key,
    code      varchar(20)  not null,
    title     varchar(200) not null,
    credits   int          not null,
    constraint code
        unique (code)
);

create table enrollments
(
    enrollment_id int auto_increment
        primary key,
    student_id    int                            not null,
    section_id    int                            not null,
    status        varchar(20) default 'enrolled' null,
    constraint student_id
        unique (student_id, section_id)
);

create table instructors
(
    user_id    int          not null
        primary key,
    department varchar(100) null
);

create table sections
(
    section_id    int auto_increment
        primary key,
    course_id     int            not null,
    instructor_id int            null,
    semester      varchar(20)    null,
    year          int            null,
    capacity      int default 30 null,
    day_time      varchar(50)    null,
    room          varchar(50)    null,
    quiz_weight   int default 20 not null,
    midsem_weight int default 30 not null,
    endsem_weight int default 50 not null,
    constraint sections_ibfk_1
        foreign key (course_id) references courses (course_id)
);

create index course_id
    on sections (course_id);

create table settings
(
    `key` varchar(50) not null
        primary key,
    value varchar(50) null
);

create table students
(
    user_id int          not null
        primary key,
    roll_no varchar(50)  null,
    program varchar(100) null,
    year    int          null
);

create table grades
(
    grade_id    int auto_increment
        primary key,
    user_id     int        not null,
    section_id  int        not null,
    quiz        int        null,
    midsem      int        null,
    endsem      int        null,
    final_grade varchar(5) null,
    constraint uq_user_section
        unique (user_id, section_id),
    constraint grades_ibfk_1
        foreign key (user_id) references students (user_id),
    constraint grades_ibfk_2
        foreign key (section_id) references sections (section_id)
);

create index section_id
    on grades (section_id);

create table registrations
(
    reg_id            int auto_increment
        primary key,
    user_id           int  not null,
    section_id        int  not null,
    registration_date date null,
    constraint user_id
        unique (user_id, section_id),
    constraint registrations_ibfk_1
        foreign key (user_id) references students (user_id),
    constraint registrations_ibfk_2
        foreign key (section_id) references sections (section_id)
);

create index section_id
    on registrations (section_id);

