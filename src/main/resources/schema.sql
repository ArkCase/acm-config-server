CREATE TABLE IF NOT EXISTS properties (
                                          id SERIAL PRIMARY KEY,
                                          created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          application TEXT,
                                          profile TEXT,
                                          label TEXT,
                                          key TEXT,
                                          value TEXT,
                                          CONSTRAINT unique_app_profile_key UNIQUE (application, profile, key)
    );
