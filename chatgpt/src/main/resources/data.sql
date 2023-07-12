DROP TABLE IF EXISTS job_listing;
DROP TABLE IF EXISTS job_experience CASCADE ;
DROP TABLE IF EXISTS cv CASCADE ;

-- Create the JobListing table
-- Create the JobListing table
CREATE TABLE job_listing
(
    id                    int          NOT NULL,
    job_title             VARCHAR(255) NOT NULL,
    company_name          VARCHAR(255) NOT NULL,
    location              VARCHAR(255) NOT NULL,
    company_description   TEXT         NOT NULL,
    job_description       TEXT         NOT NULL,
    job_responsibilities  TEXT         NOT NULL,
    job_qualifications    TEXT         NOT NULL,
    job_additional_skills TEXT         NOT NULL,
    years_of_experience   INT          NOT NULL,
    short_list_of_skills  TEXT         NOT NULL,
    employment_type       VARCHAR(255) NOT NULL,
    industry              VARCHAR(255) NOT NULL,
    salary_compensation   VARCHAR(255) NOT NULL,
    application_deadline  DATE         NOT NULL,
    contact_information   VARCHAR(255) NOT NULL,
    posting_date          DATE         NOT NULL,
    other_details         TEXT,
    PRIMARY KEY (id)
);

-- Insert test data into the JobListing table
INSERT INTO job_listing (id,
                         job_title,
                         company_name,
                         location,
                         company_description,
                         job_description,
                         job_responsibilities,
                         job_qualifications,
                         job_additional_skills,
                         years_of_experience,
                         short_list_of_skills,
                         employment_type,
                         industry,
                         salary_compensation,
                         application_deadline,
                         contact_information,
                         posting_date,
                         other_details)
VALUES (1,
        'Application Developer - Java & Web Technologies',
        'IBM',
        'Bangkok',
        'As an Application Developer, you will lead IBM into the future by translating system requirements into the ' ||
        'design and development of customized systems in an agile environment. The success of IBM is in your hands ' ||
        'as you transform vital business needs into code and drive innovation. Your work will power IBM and its clients' ||
        ' globally, collaborating and integrating code into enterprise systems. You will have access to the latest ' ||
        'education, tools and technology, and a limitless career path with the worlds technology leader.' ||
        ' Come to IBM and make a global impact!',
        'Application Developer - Java & Web Technologies',
        'You see the big picture, able to imagine successful technology-driven solutions from start to finish.' ||
        'As a Full - Stack Application Developer, you will help design, develop, and integrate solutions using best practice' ||
        'technologies, tools, techniques, and products our clients demand today. You will work with clients, cocreating ' ||
        'solutions to major real - world challenges by translating system requirements into the design and development ' ||
        'of customized systems in an agile environment.',
        'Work in an agile, collaborative environment to understand requirements, design, code and test innovative ' ||
        'applications, and support those applications for our highly valued customers. Use IBMs Design Thinking to ' ||
        'create products that provide a great user experience along with high performance, security, quality, and stability.' ||
        'Work with a variety of relational and NoSQL databases (SQL, Postgres, DB2, MongoDB), operating systems ' ||
        '(Linux, Windows, iOS, Android), and modern UI frameworks (Backbone.js, AngularJS, React.js, Ember.js, Bootstrap, and JQuery).' ||
        'Work across the entire system architecture, from backend to front end, to design, develop, and support high ' ||
        'quality and scalable products. Design and develop a new modern application or sustaining a legacy application.' ||
        'Translate software requirements into working and maintainable solutions within the existing application frameworks' ||
        'Identify bottlenecks and bugs, and devise solutions to these problems' ||
        'Support Code Review process  tools, troubleshooting, performance tuning' ||
        'Help maintain and improve code quality, organization, and automatization' ||
        'Create everything from mockups and UI components to algorithms and data structures as you deliver a complete minimally viable product.',
        'Have flexibility to work across architecture - building both front end and backend' ||
        'Proficient in one of the programming languages like Java, Ruby, Python, Javascript, HTML, CSS' ||
        'Have knowledge of modern frameworks such as Angular or React.js' ||
        'Familiar with Java JEE best practices around design patterns, performance tuning, automated tests and unit tests' ||
        'Exposure in writing RESTful APIs using Microservices architecture and Spring Boot framework ' ||
        'Exposure to deploying applications with container technology like Docker and container orchestration technology ' ||
        'like Kubernetes or RedHat OpenShift is highly desired' ||
        'Familiar with twelve-factor cloud design principles' ||
        'Experience in following a structure methodology' ||
        'Strong interpersonal skills with ability to collaborate and work effectively with individuals, strengthening relationships to achieve win-win solutions' ||
        'Ability to communicate complex situations clearly and simply by listening actively and conveying difficult messages in a positive manner' ||
        'A passion for innovative ideas, coupled with the ability to understand and assimilate different points of view' ||
        'Ability to translate business requirements into technical solutions' ||
        'Ability to thrive in as ever changing, technology based consulting environment, using agile development techniques' ||
        'Have exposure to Cloud - AWS, Azure, Google Could or IBM Cloud',
        5,
        'Java, Python, SQL',
        'Full-time',
        'Technology',
        'Competitive',
        '2023-07-31',
        '',
        '2023-07-08',
        'Other details for job listing 1'),
       (2,
        'Java Programmer',
        'G-ABLE Group',
        'Singapore',
        'We help organizations across industries create the innovations that matter with our IT and Digital expertise.' ||
        ' To reach this, we partner with our clients at every level of their organization to strategize the idea, ' ||
        'build the platform, operate the business and eventually create the greater experience to society in a ' ||
        'sustainable way. We continually seek greater ways to serve them by developing a new range of products and ' ||
        'services from IT infrastructure, enterprise application, through digital solutions. With exceptional strategy,' ||
        ' technology and creative teams from over 1,500 employees, we combine global practices from our alliances ' ||
        'with global leading companies and local executions from our comprehensive experience to help clients achieve ' ||
        'the ambitious goals.',
        'Job description for Data Analyst position.',
        'Develop software applications that meet the clients requirements ' ||
        'Thoroughly test the functionality of the self-developed software to minimize errors' ||
        'Thoroughly analyze the problems that will occur with the software and look at the impacts on the developed software ' ||
        'rationally to prevent problems that may occur in the future' ||
        'Accurately understand the scope of work assigned by System Analyst or Programmer Analyst.' ||
        'Notify problems or update status to project administrators supervisors in a timely manner.' ||
        'Improve and solve problems that occur with the software so that it can work properly and efficiently to respond to customer needs',
        'Bachelors Degree in Computer Science, Software Engineering or related field.' ||
        'At least 1-5 years of experience in Java Programming' ||
        'Strong background in Java Spring Boot' ||
        'Experience in jQuery, AngularJS React, Vue, JavaScript, Bootstrap' ||
        'Knowledge of conception of Object Oriented Design and UML Design.' ||
        'Knowledge of SQL Command and familiar with Oracle database, MSSQL Server database or any RDBMS.' ||
        'Knowledge of version controls such as Git or Subversion.' ||
        'Experience in JBOSS, Apache Tomcat, WebSphere application server is an advantage' ||
        'Experience in Jasper or Crystal Report is an advantage' ||
        'Experience in Finance or Lending is an advantage',
        'Additional skills for Data Analyst position.',
        1,
        'SQL, Excel, Data Visualization',
        'Full-time',
        'Analytics',
        'Negotiable',
        '2023-08-15',
        'Peerapat Techamaneerat',
        '2023-07-08',
        'Other details for job listing 2');


-- Create cv table
CREATE TABLE cv
(
    id                  SERIAL PRIMARY KEY,
    app_user_id         INTEGER, /* Assuming the id column in app_user is integer */
    full_name           VARCHAR(255) NOT NULL,
    contact_information TEXT,
    summary_objective   TEXT,
    soft_skills         TEXT,
    education_history   TEXT,
    hard_skills         TEXT,
    certifications      TEXT,
    projects            TEXT,
    awards_achievements TEXT,
    references_info     TEXT,
    linkedin_url        TEXT,
    github_url          TEXT,
    FOREIGN KEY (app_user_id) REFERENCES app_user(id)
);

-- Insert test data into cv table
INSERT INTO cv (id, app_user_id, full_name, contact_information, summary_objective, soft_skills,
                education_history, hard_skills, certifications, projects,
                awards_achievements,
                references_info,
                linkedin_url,
                github_url)
VALUES (1, 1,'Anton Tkach', 'anton.tk@gmail.com',
        'Highly skilled Java Developer with almost 3 years of experience in commercial projects.' ||
        'Passionate about solving complex problems and continuously learning new technologies.' ||
        'Seeking a challenging role to utilize my technical expertise in Java, Spring, Hibernate, and RESTful API development',
        'Proficient in writing efficient and maintainable code, emphasizing code reuse and functionality optimization' ||
        'Team player with good interpersonal/communication skills' ||
        'Independent thinking, able to take the initiative to identify problems, systematic problem analysis, and problem-solving skills' ||
        'Motivated self-starter with leadership and management skills' ||
        'Agile Mindset and is an advocate for promoting the culture surrounding it',
        'Master of Computer Science - Peter The Great St. Petersburg Polytechnic University (1997-2023)',
        'Java, Spring Framework (Spring Security, Spring MVC, Spring Data JPA), Hibernate ORM, RESTful API, JSON,' ||
        ' Jackson, JSP, JSTL, Apache Tomcat, RabbitMQ, WebJars, DataTables, EHCACHE PostgreSQL, HSQLDB, JUnit 5, ' ||
        'Hamcrest, AssertJ, jQuery, jQuery plugins, Bootstrap, HTML, CSS, XML Containers (Docker), CI/CD, Bash, Unix ' ||
        'command line',
        '',
        '',
        'Presales Engineer of the Year Award (Netwrix Corporation)',
        'Jane Smith (Manager) - jane@example.com',
        'linkedin.com/in/anton-tkatch',
        'github.com/BusyDizzy');

-- Create job_experience table
CREATE TABLE job_experience
(
    id           SERIAL PRIMARY KEY,
    cv_id        INT          NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    job_title    VARCHAR(255) NOT NULL,
    start_date   DATE,
    end_date     DATE,
    description  TEXT,
    CONSTRAINT fk_cv
        FOREIGN KEY (cv_id)
            REFERENCES cv (id)
);

-- Insert test data into job_experience table
INSERT INTO job_experience (id, cv_id, company_name, job_title, start_date, end_date, description)
VALUES (1, 1, 'Java Online Projects',
        'Java Developer',
        '2023-01-01',
        '2023-05-30',
        'Participated in team project developing Spring/JPA Enterprise application on the following stack: Maven/ Spring MVC/ Security/ REST(Jackson)/ Java 8 Stream API:' ||
        'Contributed to the development of a Spring/JPA Enterprise application with authorization and access rights based on roles' ||
        'Implemented persistence using Postgres and HSQLDB, utilizing Spring JDBC, JPA (Hibernate), and Spring Data JPA' ||
        'Developed REST and AJAX controllers and created an extensive JUnit test suite');

INSERT INTO job_experience (id, cv_id, company_name, job_title, start_date, end_date, description)
VALUES (2, 1, 'MindfulMentor',
        'Java Developer',
        '2022-02-01', '2023-01-01',
        'In a team of 5 developers and under the guidance of the Product Manager, I played a crucial role in delivering' ||
        ' high-quality software solutions. Here are my key achievements:Implemented and maintained RESTful APIs using ' ||
        'Spring Boot, Spring Data JPA, and Hibernate ORM, ensuring efficient data management and seamless integration.' ||
        'Assisted in developing front-end components using JSP, JSTL, jQuery, and Bootstrap, creating a visually ' ||
        'appealing and user-friendly interface.Collaborated with the team to design and optimize database schemas, ' ||
        'utilizing PostgreSQL for data storage and retrieval.Conducted thorough unit testing using JUnit 5, Hamcrest,' ||
        ' and AssertJ, ensuring the reliability and functionality of the developed features.Integrated third-party' ||
        ' libraries and plugins such as WebJars, DataTables, and jQuery plugins to enhance the platforms ' ||
        'functionality and user experience');

INSERT INTO job_experience (id, cv_id, company_name, job_title, start_date, end_date, description)
VALUES (3, 1, 'StudyRoom',
        'Junior Java Developer',
        '2020-11-01', '2022-02-01',
        'Collaborated in the design, development, and maintenance of a video conferencing application using Java, ' ||
        'Spring Boot, Hibernate, PostgreSQL, Swagger, and RESTful APIs.Worked closely with cross-functional teams ' ||
        'to deliver high-quality software solutions.Conducted code reviews, ensuring code quality standards.' ||
        'Developed and maintained automated tests to ensure code quality and minimize regression issues.' ||
        'Participated in agile development methodologies, including daily stand-ups, sprint planning, and retrospectives');
-- Add more test data as needed

