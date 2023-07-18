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


