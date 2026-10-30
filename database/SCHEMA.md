# Future database design proposal (documentation only)

No database connection, driver, ORM, repository, migration, or persistent storage is configured in this MVP.

## Proposed entities

- **User** (`id`, `email` unique, `password_hash`, `full_name`, `role_id`, `created_at`, `status`)
- **Role** (`id`, `name` unique: STUDENT, MENTOR, ADMIN)
- **StudentProfile** (`user_id` PK/FK, `specialization`, `academic_level`, `location`, `bio`)
- **MentorProfile** (`user_id` PK/FK, `specialization`, `years_experience`, `availability_summary`)
- **Course** (`id`, `title`, `description`, `level`, `duration_hours`, `published`)
- **LearningPath** (`id`, `title`, `description`, `specialization`, `duration_weeks`)
- **LearningPathCourse** (`path_id`, `course_id`, `position`) for many-to-many ordering
- **LearningProgress** (`user_id`, `course_id`, `status`, `percent_complete`, `last_activity_at`)
- **Roadmap** (`id`, `user_id`, `goal`, `provider`, `content_json`, `created_at`)
- **Resource** (`id`, `title`, `category`, `source`, `url`, `verified_at`)
- **Opportunity** (`id`, `title`, `organization`, `type`, `eligibility_summary`, `official_url`, `deadline`, `verified_at`)
- **MentorshipSession** (`id`, `mentor_user_id`, `student_user_id`, `starts_at`, `status`, `notes`)
- **SavedItem** (`id`, `user_id`, `item_type`, `item_id`, `created_at`)

## Relationships

- Role 1:N User.
- User 1:0..1 StudentProfile and 1:0..1 MentorProfile, based on role.
- LearningPath M:N Course through LearningPathCourse.
- User M:N Course through LearningProgress.
- User 1:N Roadmap and SavedItem.
- Mentor User 1:N MentorshipSession; student User 1:N MentorshipSession.

Use foreign keys, unique email constraints, timestamps, indexes for role and status, and audited administrative changes when implementing persistence later. Store source verification timestamps for opportunities and resources rather than implying a URL guarantees a current opening.
