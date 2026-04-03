UPDATE users
SET active = COALESCE(active, true),
    created_date = COALESCE(created_date, CURRENT_TIMESTAMP),
    updated_date = COALESCE(updated_date, CURRENT_TIMESTAMP)
WHERE active IS NULL
   OR created_date IS NULL
   OR updated_date IS NULL;
