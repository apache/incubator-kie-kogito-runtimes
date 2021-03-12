CREATE TABLE public.process_instances(id uuid NOT NULL,
                                      payload bytea,
                                      process_id character varying NOT NULL,
                                      CONSTRAINT process_instances_pkey PRIMARY KEY (id)
                                      )