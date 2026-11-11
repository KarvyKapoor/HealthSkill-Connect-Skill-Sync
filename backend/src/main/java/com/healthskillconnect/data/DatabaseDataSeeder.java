package com.healthskillconnect.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthskillconnect.model.*;
import com.healthskillconnect.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DatabaseDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseDataSeeder.class);

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final CareerPathRepository careerPathRepository;
    private final OpportunityRepository opportunityRepository;
    private final PostRepository postRepository;
    private final NoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DatabaseDataSeeder(
            UserRepository userRepository,
            SkillRepository skillRepository,
            CareerPathRepository careerPathRepository,
            OpportunityRepository opportunityRepository,
            PostRepository postRepository,
            NoteRepository noteRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.careerPathRepository = careerPathRepository;
        this.opportunityRepository = opportunityRepository;
        this.postRepository = postRepository;
        this.noteRepository = noteRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedSkills();
        seedCareerPaths();
        seedUsers();
        seedOpportunities();
        seedPosts();
        seedInitialNotes();
        log.info("DatabaseDataSeeder completed successfully. MySQL database is primed with healthcare taxonomy, careers, opportunities, and users.");
    }

    private void seedSkills() {
        if (skillRepository.count() > 0) return;
        List<Skill> skills = List.of(
                // Clinical Knowledge
                new Skill("skill-clin-trials", "Clinical Trials", "Clinical Knowledge", "Principles of human clinical trial design, phases, and protocol compliance.", "Clinical Research, MBBS, Pharmacy", "Clinical Research Associate, Clinical Project Manager"),
                new Skill("skill-med-term", "Medical Terminology", "Clinical Knowledge", "Standardized anatomical, pathological, and clinical vocabulary.", "All Healthcare Fields", "Medical Coder, Clinical Documentation Specialist"),
                new Skill("skill-patient-safety", "Patient Safety Fundamentals", "Clinical Knowledge", "Safety culture, incident reporting, human factors, and infection control.", "Nursing, MBBS, Allied Health", "Healthcare Quality Analyst, Clinical Nurse Specialist"),
                new Skill("skill-ebm", "Evidence-Based Medicine", "Clinical Knowledge", "Systematic critical appraisal and application of biomedical research.", "MBBS, Public Health, Pharmacy", "Clinical Researcher, Medical Science Liaison"),
                new Skill("skill-pharmacology", "Clinical Pharmacology", "Clinical Knowledge", "Pharmacodynamics, pharmacokinetics, drug interactions, and toxicity.", "Pharmacy, MBBS, Nursing", "Pharmacovigilance Associate, Clinical Pharmacist"),

                // Laboratory & Diagnostics
                new Skill("skill-lab-microbio", "Microbiology & Staining", "Laboratory & Diagnostics", "Diagnostic culturing, microscopy, antibiotic susceptibility testing.", "Medical Lab Tech, Life Sciences", "Medical Laboratory Technologist, Microbiologist"),
                new Skill("skill-pathology", "Clinical Pathology", "Laboratory & Diagnostics", "Analysis of blood, fluid specimens, hematology, and clinical chemistry.", "Medical Lab Tech, MBBS", "Laboratory Scientist, Pathologist Assistant"),
                new Skill("skill-molecular-diag", "Molecular Diagnostics", "Laboratory & Diagnostics", "PCR, DNA extraction, sequencing technologies, and genetic assay workflows.", "Biotech, Biomedical Science", "Molecular Diagnostic Specialist, Genomics Researcher"),

                // Health Informatics & Technology
                new Skill("skill-health-analytics", "Healthcare Data Analytics", "Health Informatics & Tech", "Processing health metrics, outcomes analysis, and descriptive biostatistics.", "Health Informatics, Computer Science, Public Health", "Healthcare Data Analyst, Biostatistician"),
                new Skill("skill-ehr", "Electronic Health Records (EHR)", "Health Informatics & Tech", "Clinical record systems, Epic/Cerner workflows, and patient data compliance.", "Nursing, Health Admin, Informatics", "Health Informatics Specialist, Clinical Informatician"),
                new Skill("skill-med-coding", "Medical Coding (ICD-10 & CPT)", "Health Informatics & Tech", "Classification of clinical diagnoses, procedures, and healthcare billing rules.", "Life Sciences, Nursing, BPharm", "Medical Coder, Billing Specialist"),
                new Skill("skill-python-health", "Python for Biomedical Data", "Health Informatics & Tech", "Pandas, NumPy, and statistical pipelines for healthcare datasets.", "Informatics, Biomedical Engineering", "Health Data Scientist, Bioinformatics Analyst"),

                // Research & Epidemiology
                new Skill("skill-gcp", "Good Clinical Practice (GCP)", "Research & Epidemiology", "International ethical and quality standards for clinical trials on humans.", "Clinical Research, Pharmacy, MBBS", "Clinical Research Associate, Regulatory Affairs Officer"),
                new Skill("skill-cdm", "Clinical Data Management", "Research & Epidemiology", "CRF design, data cleaning, validation, and database lock procedures.", "Life Sciences, Pharmacy, Informatics", "Clinical Data Manager, Data Coordinator"),
                new Skill("skill-epidemiology", "Epidemiological Surveillance", "Research & Epidemiology", "Disease outbreak investigation, incidence modeling, and population surveillance.", "Public Health, MBBS", "Public Health Epidemiologist, Biostatistician"),
                new Skill("skill-research-ethics", "Research Ethics & IRB Protocol", "Research & Epidemiology", "Informed consent, Institutional Review Board compliance, and participant safeguards.", "Clinical Research, Ethics, Public Health", "IRB Coordinator, Research Administrator"),

                // Healthcare Administration & Quality
                new Skill("skill-hosp-ops", "Hospital Operations & Workflow", "Healthcare Administration", "Inpatient flow, clinical scheduling, resource management, and regulatory compliance.", "Healthcare Management, Nursing", "Hospital Operations Executive, Clinical Administrator"),
                new Skill("skill-health-quality", "Healthcare Quality Improvement", "Healthcare Administration", "Lean healthcare, Six Sigma, clinical audit methodologies, and NABH/JCI standards.", "Nursing, MBBS, Healthcare Admin", "Quality Assurance Specialist, Patient Safety Officer"),

                // Patient Care & Communication
                new Skill("skill-patient-comm", "Empathetic Patient Communication", "Patient Care & Safety", "De-escalation, patient education, cultural competence, and shared decision-making.", "Nursing, Medicine, Physiotherapy", "Registered Nurse, Patient Advocate"),
                new Skill("skill-rehab", "Physical Rehabilitation Techniques", "Patient Care & Safety", "Therapeutic exercise prescription, kinesiology, and functional restoration.", "Physiotherapy, Allied Health", "Physiotherapist, Rehabilitation Consultant")
        );
        skillRepository.saveAll(skills);
        log.info("Seeded {} healthcare skills.", skills.size());
    }

    private void seedCareerPaths() {
        if (careerPathRepository.count() > 0) return;
        List<CareerPath> careers = List.of(
                new CareerPath(
                        "career-cra",
                        "Clinical Research Associate (CRA)",
                        "Clinical Research",
                        "Monitors clinical trials at research sites to ensure adherence to protocols, Good Clinical Practice (GCP), and patient safety guidelines.",
                        "BPharm, MPharm, MBBS, BSc Nursing, MSc Life Sciences",
                        "Clinical Research, Pharmacology, Nursing, Medicine",
                        "Good Clinical Practice (GCP), Clinical Trials, Medical Terminology, Clinical Data Management, Research Ethics & IRB Protocol",
                        "CCRA (Certified Clinical Research Associate), GCP Certification",
                        "₹4.5 - ₹9.0 LPA (Entry/Mid)",
                        "High Growth (14% annual expansion in CRO sector)"
                ),
                new CareerPath(
                        "career-data-analyst",
                        "Healthcare Data Analyst",
                        "Health Informatics",
                        "Analyzes patient outcomes, clinical trial registries, and hospital performance metrics using statistical software and business intelligence tools.",
                        "BSc/MSc Health Informatics, Biostatistics, Biotechnology, Allied Health with Analytics, BPharm",
                        "Health Informatics, Healthcare Analytics, Biotechnology, Public Health",
                        "Healthcare Data Analytics, Electronic Health Records (EHR), Python for Biomedical Data, Medical Coding (ICD-10 & CPT)",
                        "CHDA (Certified Health Data Analyst), Google Data Analytics",
                        "₹5.0 - ₹11.0 LPA (Entry/Mid)",
                        "Very High Demand (Crucial for evidence-based hospital administration)"
                ),
                new CareerPath(
                        "career-pharmacovigilance",
                        "Pharmacovigilance Associate",
                        "Pharmacology & Safety",
                        "Monitors and assesses adverse drug reactions (ADRs), prepares safety narrative reports, and ensures regulatory compliance with global health agencies.",
                        "BPharm, PharmD, MBBS, BDS, BSc Nursing",
                        "Pharmacy, Pharmacology, Clinical Medicine",
                        "Clinical Pharmacology, Medical Terminology, Good Clinical Practice (GCP), Evidence-Based Medicine",
                        "Certified Pharmacovigilance Professional (CPVP)",
                        "₹4.0 - ₹8.0 LPA",
                        "High Demand across multinational pharmaceutical and CRO companies"
                ),
                new CareerPath(
                        "career-lab-technologist",
                        "Medical Laboratory Technologist",
                        "Laboratory & Diagnostics",
                        "Performs complex diagnostic analysis on patient specimens, operates automated analyzers, and ensures diagnostic quality control.",
                        "BSc Medical Laboratory Technology (BMLT), MSc Clinical Biochemistry/Microbiology",
                        "Medical Lab Technology, Microbiology, Pathology",
                        "Microbiology & Staining, Clinical Pathology, Molecular Diagnostics, Patient Safety Fundamentals",
                        "ASCP Medical Laboratory Scientist (MLS), NABL Quality Auditor",
                        "₹3.5 - ₹7.0 LPA",
                        "Stable & Essential across hospital networks and diagnostic chains"
                ),
                new CareerPath(
                        "career-epidemiologist",
                        "Public Health Epidemiologist",
                        "Public Health",
                        "Investigates patterns and causes of disease in populations, designs surveillance systems, and shapes health policy interventions.",
                        "MPH (Master of Public Health), MBBS, MSc Biostatistics/Life Sciences",
                        "Public Health, Epidemiology, Community Medicine",
                        "Epidemiological Surveillance, Evidence-Based Medicine, Healthcare Data Analytics, Research Ethics & IRB Protocol",
                        "CPH (Certified in Public Health), Field Epidemiology Training (FETP)",
                        "₹5.5 - ₹12.0 LPA",
                        "Growing rapidly with national public health missions and global NGO funding"
                ),
                new CareerPath(
                        "career-medical-coder",
                        "Medical Coder & Health Information Specialist",
                        "Health Informatics",
                        "Translates patient medical records into standardized classification codes (ICD-10-CM, CPT, HCPCS) for clinical documentation and billing.",
                        "BSc Nursing, BPharm, BSc Life Sciences, Any Healthcare Degree/Diploma",
                        "Nursing, Pharmacy, Biomedical Sciences",
                        "Medical Coding (ICD-10 & CPT), Medical Terminology, Electronic Health Records (EHR)",
                        "CPC (Certified Professional Coder) by AAPC, CCS by AHIMA",
                        "₹3.6 - ₹7.5 LPA",
                        "Consistent High Demand in international healthcare RCM enterprises"
                ),
                new CareerPath(
                        "career-quality-analyst",
                        "Healthcare Quality & Safety Analyst",
                        "Healthcare Administration",
                        "Evaluates hospital clinical workflows, tracks infection control indices, and leads accreditation audits (NABH, JCI).",
                        "BSc/MSc Nursing, MHA (Master of Hospital Admin), MBBS",
                        "Nursing, Hospital Administration, Clinical Medicine",
                        "Healthcare Quality Improvement, Patient Safety Fundamentals, Hospital Operations & Workflow, Electronic Health Records (EHR)",
                        "CPHQ (Certified Professional in Healthcare Quality), Lean Six Sigma Green Belt",
                        "₹4.5 - ₹10.0 LPA",
                        "Expanding with private healthcare hospital accreditation mandates"
                )
        );
        careerPathRepository.saveAll(careers);
        log.info("Seeded {} healthcare career paths.", careers.size());
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;
        String passHash = passwordEncoder.encode("DemoPass123!");

        // Student 1: Aarav Sharma (Nursing)
        AppUser aarav = new AppUser("user-001", "Aarav Sharma", "aarav.student@example.com", "STUDENT", "BSc Nursing", "Year 3", "Delhi");
        aarav.passwordHash = passHash;
        aarav.institution = "All India Institute of Medical Sciences (AIIMS)";
        aarav.degree = "BSc (Hons) Nursing";
        aarav.headline = "Aspiring Clinical Nurse Specialist & Healthcare Quality Advocate";
        aarav.bio = "Third-year nursing student passionate about evidence-based patient safety, infection control, and clinical research methodologies.";
        aarav.skills = new ArrayList<>(List.of("Patient Safety Fundamentals", "Empathetic Patient Communication", "Medical Terminology"));
        aarav.learningInterests = new ArrayList<>(List.of("Patient Care & Safety", "Healthcare Quality", "Clinical Research"));
        aarav.learningProgress = 55;
        aarav.onboardingCompleted = true;
        aarav.verificationStatus = "VERIFIED";

        // Student 2: Meera Iyer (MBBS)
        AppUser meera = new AppUser("user-002", "Meera Iyer", "meera.student@example.com", "STUDENT", "MBBS", "Year 4", "Chennai");
        meera.passwordHash = passHash;
        meera.institution = "Madras Medical College";
        meera.degree = "MBBS";
        meera.headline = "Final Year Medical Student | Clinical Research & Cardiology";
        meera.bio = "Focusing on clinical cardiology trials, systematic reviews, and public health data analytics.";
        meera.skills = new ArrayList<>(List.of("Evidence-Based Medicine", "Clinical Trials", "Medical Terminology"));
        meera.learningInterests = new ArrayList<>(List.of("Clinical Research", "Cardiology", "Biostatistics"));
        meera.learningProgress = 72;
        meera.onboardingCompleted = true;
        meera.verificationStatus = "VERIFIED";

        // Mentor 1: Dr. Nisha Rao (Public Health Educator)
        AppUser nisha = new AppUser("user-006", "Dr. Nisha Rao", "nisha.mentor@example.com", "MENTOR", "Public Health", "8 years", "Bengaluru");
        nisha.passwordHash = passHash;
        nisha.institution = "National Institute of Epidemiology";
        nisha.degree = "MD Community Medicine, MPH";
        nisha.headline = "Assistant Professor of Epidemiology | Healthcare Educator & Mentor";
        nisha.bio = "Guiding healthcare students and early career researchers in study design, outbreak surveillance, and evidence appraisal.";
        nisha.skills = new ArrayList<>(List.of("Epidemiological Surveillance", "Evidence-Based Medicine", "Research Ethics & IRB Protocol"));
        nisha.learningInterests = new ArrayList<>(List.of("Epidemiology", "Global Health", "Health Policy"));
        nisha.verificationStatus = "VERIFIED";
        nisha.credentialsSummary = "MD Community Medicine, ICMR Principal Investigator, 15+ PubMed Indexed Papers";

        // Mentor 2: Dr. Arjun Menon (Clinical Research Professional)
        AppUser arjun = new AppUser("user-007", "Dr. Arjun Menon", "arjun.mentor@example.com", "MENTOR", "Clinical Research", "10 years", "Pune");
        arjun.passwordHash = passHash;
        arjun.institution = "Serum Institute & BioPharma Research";
        arjun.degree = "PhD Clinical Pharmacology";
        arjun.headline = "Lead Clinical Development Scientist | Industry Mentor";
        arjun.bio = "Mentoring healthcare graduates transitioning into Clinical Trials, GCP Compliance, and Pharmacovigilance careers.";
        arjun.skills = new ArrayList<>(List.of("Clinical Trials", "Good Clinical Practice (GCP)", "Clinical Pharmacology"));
        arjun.learningInterests = new ArrayList<>(List.of("Drug Discovery", "Regulatory Affairs", "Bioethics"));
        arjun.verificationStatus = "VERIFIED";
        arjun.credentialsSummary = "PhD Pharmacology, Certified Clinical Research Professional (CCRP)";

        // Healthcare Professional: Dr. Vikram Sen
        AppUser vikram = new AppUser("user-010", "Dr. Vikram Sen", "dr.vikram.cardio@example.com", "HEALTHCARE_PROFESSIONAL", "Cardiology", "12 years", "Mumbai");
        vikram.passwordHash = passHash;
        vikram.institution = "Tata Memorial Centre";
        vikram.degree = "DM Cardiology, MD Medicine";
        vikram.headline = "Interventional Cardiologist & Clinical Researcher";
        vikram.bio = "Active clinician engaged in transcatheter cardiac interventions, preventive cardiology protocols, and medical teaching.";
        vikram.skills = new ArrayList<>(List.of("Evidence-Based Medicine", "Clinical Trials", "Empathetic Patient Communication"));
        vikram.learningInterests = new ArrayList<>(List.of("Cardiac Therapeutics", "Health Tech", "Clinical Guidance"));
        vikram.verificationStatus = "VERIFIED";
        vikram.credentialsSummary = "Medical Council of India Verified, Fellow American College of Cardiology (FACC)";

        // Recruiter: Apollo Hospitals
        AppUser recruiter = new AppUser("user-011", "Apollo Healthcare Careers", "recruiter@apollo.example.com", "RECRUITER", "Talent Acquisition", "6 years", "Hyderabad");
        recruiter.passwordHash = passHash;
        recruiter.institution = "Apollo Hospitals Enterprise Ltd.";
        recruiter.headline = "Lead Clinical & Allied Health Talent Acquisition Specialist";
        recruiter.bio = "Connecting qualified nursing, pharmacy, allied health, and medical research graduates with premier clinical positions across Apollo network.";
        recruiter.verificationStatus = "VERIFIED";

        // Admin: Platform Admin
        AppUser admin = new AppUser("user-009", "Platform Administrator", "admin@example.com", "ADMIN", "Platform Operations", "5 years", "Remote");
        admin.passwordHash = passHash;
        admin.headline = "HealthSkill Connect Operations & Compliance Lead";
        admin.verificationStatus = "VERIFIED";

        userRepository.saveAll(List.of(aarav, meera, nisha, arjun, vikram, recruiter, admin));
        log.info("Seeded demo users across all roles (Student, Mentor, Professional, Recruiter, Admin).");
    }

    private void seedOpportunities() {
        if (opportunityRepository.count() > 0) return;
        List<Opportunity> opps = List.of(
                new Opportunity(
                        "opp-who-internship",
                        "WHO Health Policy & Research Internship",
                        "World Health Organization (WHO)",
                        "INTERNSHIP",
                        "Gain practical exposure to international public health programmes, technical guidelines formulation, and epidemiological surveillance.",
                        "Enrolled in or completed Undergraduate/Postgraduate in Medicine, Public Health, Nursing, Pharmacy, or Allied Sciences.",
                        "Public Health, Nursing, Medicine, Pharmacy",
                        "[\"Epidemiological Surveillance\", \"Evidence-Based Medicine\", \"Research Ethics & IRB Protocol\"]",
                        "Geneva / Regional Offices (Hybrid)",
                        "HYBRID",
                        "6 months",
                        "Stipend provided per WHO guidelines",
                        "2026-12-15",
                        "https://www.who.int/careers/internship-programme",
                        "VERIFIED"
                ),
                new Opportunity(
                        "opp-icmr-jrf",
                        "ICMR Junior Research Fellowship in Clinical Epidemiology",
                        "Indian Council of Medical Research (ICMR)",
                        "RESEARCH",
                        "Hands-on clinical study monitoring, patient registry analysis, and multi-centric field trial documentation.",
                        "Master's or Bachelor's in Life Sciences, BPharm, MBBS, MSc Nursing with minimum 55% marks.",
                        "Clinical Research, Epidemiology, Life Sciences",
                        "[\"Clinical Trials\", \"Good Clinical Practice (GCP)\", \"Healthcare Data Analytics\"]",
                        "New Delhi & Field Sites",
                        "ON_SITE",
                        "12 months (Renewable)",
                        "₹37,000 / month + HRA",
                        "2026-11-30",
                        "https://www.icmr.gov.in/",
                        "VERIFIED"
                ),
                new Opportunity(
                        "opp-apollo-nurse-residency",
                        "Graduate Clinical Nurse Residency & Quality Fellowship",
                        "Apollo Hospitals",
                        "CLINICAL_PLACEMENT",
                        "Intensive clinical transition programme covering critical care, infection control audits, and patient safety protocols.",
                        "BSc Nursing or GNM from a recognized nursing council with state registration.",
                        "Nursing, Critical Care",
                        "[\"Patient Safety Fundamentals\", \"Empathetic Patient Communication\", \"Medical Terminology\"]",
                        "Bengaluru / Chennai / Hyderabad",
                        "ON_SITE",
                        "12 months",
                        "₹28,000 - ₹34,000 / month",
                        "2026-12-01",
                        "https://www.apollohospitals.com/careers/",
                        "VERIFIED"
                ),
                new Opportunity(
                        "opp-novartis-pv",
                        "Pharmacovigilance & Drug Safety Associate",
                        "Novartis Healthcare",
                        "FULL_TIME",
                        "Process adverse drug event reports, draft ICSR safety narratives, and monitor regulatory safety signal alerts.",
                        "BPharm, MPharm, PharmD, BDS, or BSc Nursing graduates.",
                        "Pharmacy, Pharmacology, Nursing",
                        "[\"Clinical Pharmacology\", \"Good Clinical Practice (GCP)\", \"Medical Terminology\"]",
                        "Hyderabad",
                        "HYBRID",
                        "Full-Time Permanent",
                        "₹4.8 - ₹6.5 LPA",
                        "2026-11-20",
                        "https://www.novartis.com/careers",
                        "VERIFIED"
                ),
                new Opportunity(
                        "opp-aiims-data-analyst",
                        "Clinical Data Analyst Trainee",
                        "All India Institute of Medical Sciences (AIIMS)",
                        "INTERNSHIP",
                        "Assist clinical departments in data extraction, statistical analysis of surgical registries, and EHR visualization.",
                        "Degree in Health Informatics, Biostatistics, Biotechnology, or Allied Sciences with analytics exposure.",
                        "Health Informatics, Biostatistics, Computer Science",
                        "[\"Healthcare Data Analytics\", \"Electronic Health Records (EHR)\", \"Python for Biomedical Data\"]",
                        "New Delhi",
                        "ON_SITE",
                        "6 months",
                        "₹25,000 / month",
                        "2026-12-10",
                        "https://www.aiims.edu/",
                        "VERIFIED"
                )
        );
        opportunityRepository.saveAll(opps);
        log.info("Seeded {} verified healthcare opportunities.", opps.size());
    }

    private void seedPosts() {
        if (postRepository.count() > 0) return;
        List<Post> posts = List.of(
                new Post(
                        "user-006",
                        "Dr. Nisha Rao",
                        "MENTOR",
                        "Public Health",
                        "NR",
                        "How to Critically Appraise a Clinical Study: A 4-Step Checklist for Students",
                        "When reading newly published biomedical literature, don't just accept the abstract conclusion. Always verify:\n\n1. Study Design: Was it an RCT, prospective cohort, or retrospective review? Hierarchy of evidence matters.\n2. Sample Size & Power: Was the sample adequately powered to detect a true clinical difference?\n3. Primary vs Secondary Outcomes: Are the authors highlighting secondary findings because the primary outcome was negative?\n4. Funding & Conflict of Interest: Always check disclosures.\n\nDeveloping this habit early will dramatically sharpen your clinical reasoning!",
                        "CLINICAL_INSIGHT",
                        "[\"Evidence-Based Medicine\", \"Study Design\", \"Clinical Research\"]"
                ),
                new Post(
                        "user-010",
                        "Dr. Vikram Sen",
                        "HEALTHCARE_PROFESSIONAL",
                        "Cardiology",
                        "VS",
                        "Patient Communication in High-Stress Clinical Scenarios",
                        "Yesterday in the cardiac care unit, a family was overwhelmed by sudden clinical jargon. Here is a golden rule for medical and nursing students:\n\nAvoid speaking in acronyms. Instead of saying 'He has STEMI with EF 30%', explain 'A main blood vessel to the heart has a blockage and the pumping muscle is currently working with reduced strength.'\n\nClarity reduces anxiety. Patient education is 50% of the therapy.",
                        "CASE_STUDY",
                        "[\"Patient Communication\", \"Cardiology\", \"Clinical Practice\"]"
                ),
                new Post(
                        "user-007",
                        "Dr. Arjun Menon",
                        "MENTOR",
                        "Clinical Research",
                        "AM",
                        "Transitioning from Pharmacy & Nursing into Clinical Research (CRA Careers)",
                        "A question I get asked frequently by healthcare learners: 'Can I become a Clinical Research Associate right after graduation?'\n\nYes! But you must build proof of competency beyond your classroom degree:\n- Complete GCP (Good Clinical Practice) certification\n- Understand how an electronic Trial Master File (eTMF) functions\n- Learn the essentials of protocol deviation tracking\n\nCheck out the CRA career roadmap on this platform—it aligns directly with what our hiring teams evaluate.",
                        "CAREER_ADVICE",
                        "[\"Clinical Research\", \"Career Guidance\", \"CRA\"]"
                )
        );
        postRepository.saveAll(posts);
        log.info("Seeded {} professional networking posts.", posts.size());
    }

    private void seedInitialNotes() {
        if (noteRepository.count() > 0) return;
        Note sampleNote = new Note(
                "user-001",
                "Patient Safety Fundamentals - Clinical Study Notes",
                "Patient safety",
                "Clinical practice",
                "Beginner",
                "Core overview of clinical safety culture and incident mitigation in hospital environments.",
                "[{\"heading\":\"Overview\",\"content\":\"Patient safety focuses on preventing errors and adverse effects to patients associated with healthcare delivery.\"},{\"heading\":\"Key Principles\",\"content\":\"1. Just Culture vs Blame Culture\\n2. The Swiss Cheese Model of Accident Causation\\n3. Two-Identifier Verification Protocol\\n4. High-Alert Medication Double-Checks\"}]",
                "[{\"question\":\"What is the primary objective of Root Cause Analysis (RCA)?\",\"answer\":\"To identify underlying system deficiencies rather than assigning individual blame.\"}]",
                "[\"Patient Safety\", \"Clinical Audit\", \"Nursing\"]",
                false,
                "SHARED"
        );
        noteRepository.save(sampleNote);
        log.info("Seeded initial study notes.");
    }
}
