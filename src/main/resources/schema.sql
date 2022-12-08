DROP SCHEMA IF EXISTS `test`;
CREATE SCHEMA IF NOT EXISTS `test`;
 USE `test`;
Create Table course
(
courseId INTEGER(20),
courseNAmes VARCHAR(500),
PRIMARY KEY(courseId)
);
Create Table teacher
(
id BIGINT(8) Unsigned AUTO_INCREMENT,
name VARCHAR(500),
PRIMARY KEY(id)
);
Create Table Student
(
id INTEGER(10) Unsigned AUTO_INCREMENT,
StudentName VARCHAR(100),
date_of_brith DATE NOT NULL UNIQUE,
update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
image BLOB,
teacher_id BIGINT(8) Unsigned,
CONSTRAINT fk_Student_teacher_teacher_id
	FOREIGN KEY Student(teacher_id) REFERENCES teacher(id) ON UPDATE CASCADE ON DELETE NO ACTION,
PRIMARY KEY(id,StudentName)
);
Create Table rating
(
id INTEGER(10),
student_id INTEGER(10) Unsigned,
teacher_id BIGINT(8) Unsigned UNIQUE,
name VARCHAR(255),
CONSTRAINT fk_rating_Student_student_id
	FOREIGN KEY rating(student_id) REFERENCES Student(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT fk_rating_teacher_teacher_id
	FOREIGN KEY rating(teacher_id) REFERENCES teacher(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
PRIMARY KEY(id)
);
Create Table comment
(
id INTEGER(10),
student_id INTEGER(10) Unsigned UNIQUE,
rating_id INTEGER(10),
CONSTRAINT fk_comment_Student_student_id
	FOREIGN KEY comment(student_id) REFERENCES Student(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT fk_comment_rating_rating_id
	FOREIGN KEY comment(rating_id) REFERENCES rating(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
PRIMARY KEY(id)
);
Create Table student_course
(
Student_id INTEGER(10) Unsigned,
course_id INTEGER(20) UNIQUE,
CONSTRAINT fk_1_student_course
	FOREIGN KEY (Student_id) REFERENCES Student(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT fk_2_student_course
	FOREIGN KEY (course_id) REFERENCES course(courseId) ON UPDATE NO ACTION ON DELETE NO ACTION,
PRIMARY KEY (Student_id,course_id)
);
Create Table rating_course
(
rating_id INTEGER(10),
course_id INTEGER(20) UNIQUE,
CONSTRAINT fk_1_rating_course
	FOREIGN KEY (rating_id) REFERENCES rating(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT fk_2_rating_course
	FOREIGN KEY (course_id) REFERENCES course(courseId) ON UPDATE NO ACTION ON DELETE NO ACTION,
PRIMARY KEY (rating_id,course_id)
);
Create Table student_teacher
(
Student_id INTEGER(10) Unsigned,
teacher_id BIGINT(8) Unsigned,
CONSTRAINT fk_1_student_teacher
	FOREIGN KEY (Student_id) REFERENCES Student(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT fk_2_student_teacher
	FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
PRIMARY KEY (Student_id,teacher_id)
);
