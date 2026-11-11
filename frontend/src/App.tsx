import React, { useEffect, useState } from 'react';
import {
  Activity, BookOpen, Brain, BriefcaseMedical, ChevronRight, ChevronLeft,
  FileText, GraduationCap, HeartPulse, LogOut, Menu, Microscope, Presentation,
  Search, ShieldCheck, Users, X, LockKeyhole, Eye, EyeOff, ArrowUpRight,
  Compass, UserRound, CheckCircle2, LoaderCircle, Plus, Trash2, Edit3,
  Bookmark, ThumbsUp, MessageSquare, Calendar, Award, TrendingUp, Sparkles,
  Filter, Check, AlertCircle, Send, Layers, Building2, Stethoscope, SlidersHorizontal
} from 'lucide-react';
import * as API from './api';

// Demo Accounts mapping for easy one-click testing
const DEMO_ACCOUNTS = [
  { label: 'Student (Nursing)', email: 'aarav.student@example.com', role: 'STUDENT' },
  { label: 'Student (MBBS)', email: 'meera.student@example.com', role: 'STUDENT' },
  { label: 'Mentor (Epidemiology)', email: 'nisha.mentor@example.com', role: 'MENTOR' },
  { label: 'Mentor (Clinical Research)', email: 'arjun.mentor@example.com', role: 'MENTOR' },
  { label: 'Doctor (Cardiologist)', email: 'dr.vikram.cardio@example.com', role: 'HEALTHCARE_PROFESSIONAL' },
  { label: 'Recruiter (Apollo Hospitals)', email: 'recruiter@apollo.example.com', role: 'RECRUITER' },
  { label: 'Platform Admin', email: 'admin@example.com', role: 'ADMIN' },
];
const DEMO_PASSWORD = 'DemoPass123!';

const HEALTHCARE_INTERESTS_OPTIONS = [
  'Clinical healthcare', 'Nursing practice', 'Pharmacy & Therapeutics',
  'Medical laboratory technology', 'Radiology and imaging', 'Physiotherapy & Rehabilitation',
  'Public health & Epidemiology', 'Healthcare administration & Operations',
  'Clinical research & Trials', 'Biomedical sciences', 'Biotechnology',
  'Health informatics & EHR', 'Medical coding & Billing', 'Healthcare data analytics',
  'Patient safety & Quality assurance'
];

const CAREER_PREFERENCES_OPTIONS = [
  'Clinical practice & Patient care', 'Biomedical research & Clinical trials',
  'Teaching, academia & Mentoring', 'Healthcare technology & Informatics',
  'Hospital administration & Management', 'Laboratory diagnostics & Pathology',
  'Public health policy & Global health', 'Industry, pharma & Medical devices',
  'Healthcare entrepreneurship', 'Undecided / Exploring multiple paths'
];

export default function App() {
  const [user, setUser] = useState<API.AuthUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [page, setPage] = useState('Home');
  const [authMode, setAuthMode] = useState<'login' | 'signup'>('login');
  const [email, setEmail] = useState(DEMO_ACCOUNTS[0].email);
  const [password, setPassword] = useState(DEMO_PASSWORD);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  // Registration Form
  const [regForm, setRegForm] = useState({
    name: '',
    email: '',
    password: '',
    confirm: '',
    role: 'STUDENT',
    specialization: 'BSc Nursing',
    experienceLevel: 'Year 2',
    location: 'Delhi'
  });

  // Onboarding Wizard State
  const [showOnboarding, setShowOnboarding] = useState(false);
  const [onboardingStep, setOnboardingStep] = useState(1);
  const [onboardingData, setOnboardingData] = useState({
    currentQualification: 'Undergraduate',
    degreeOrCourse: 'BSc Nursing',
    specialization: 'Nursing',
    institution: 'All India Institute of Medical Sciences (AIIMS)',
    academicYearOrSemester: 'Year 3',
    healthcareInterests: ['Clinical healthcare', 'Patient safety & Quality assurance', 'Public health & Epidemiology'],
    careerPreferences: ['Clinical practice & Patient care', 'Biomedical research & Clinical trials'],
    existingSkills: [
      { name: 'Patient Safety Fundamentals', proficiency: 'Intermediate' },
      { name: 'Empathetic Patient Communication', proficiency: 'Advanced' },
      { name: 'Medical Terminology', proficiency: 'Basic' }
    ],
    experienceSummary: 'Clinical rotation in medical-surgical ward (6 months)',
    shortTermGoals: 'Master infection control audits and Good Clinical Practice guidelines.',
    longTermGoals: 'Become a certified Clinical Nurse Specialist or Quality Assurance Manager.',
    learningPreferences: ['Case-based learning', 'Interactive roadmaps', 'Self-paced revision notes']
  });

  // Data Caches
  const [skillsCatalog, setSkillsCatalog] = useState<any[]>([]);
  const [userSkills, setUserSkills] = useState<any[]>([]);
  const [careersCatalog, setCareersCatalog] = useState<any[]>([]);
  const [careerRecommendations, setCareerRecommendations] = useState<any[]>([]);
  const [marketReadiness, setMarketReadiness] = useState<any>(null);
  const [opportunities, setOpportunities] = useState<any[]>([]);
  const [oppTypeFilter, setOppTypeFilter] = useState('ALL');
  const [myApplications, setMyApplications] = useState<any[]>([]);
  const [networkFeed, setNetworkFeed] = useState<any[]>([]);
  const [feedCategoryFilter, setFeedCategoryFilter] = useState('ALL');
  const [connections, setConnections] = useState<any[]>([]);
  const [members, setMembers] = useState<any[]>([]);
  const [memberSearchQuery, setMemberSearchQuery] = useState('');
  const [memberRoleFilter, setMemberRoleFilter] = useState('ALL');
  const [mentorshipRequests, setMentorshipRequests] = useState<any[]>([]);
  const [userNotes, setUserNotes] = useState<any[]>([]);
  const [userRoadmaps, setUserRoadmaps] = useState<any[]>([]);
  const [userStudyPlans, setUserStudyPlans] = useState<any[]>([]);
  const [courses, setCourses] = useState<any[]>([]);
  const [auditLogs, setAuditLogs] = useState<any[]>([]);
  const [dashboardData, setDashboardData] = useState<any>(null);

  // AI Studio Active Tool State
  const [aiStudioTab, setAiStudioTab] = useState<'notes' | 'builder' | 'roadmap' | 'presentation' | 'study_plan' | 'explainer' | 'skill_plan'>('notes');
  const [aiTopic, setAiTopic] = useState('Clinical trials');
  const [aiLevel, setAiLevel] = useState('Intermediate');
  const [aiGoal, setAiGoal] = useState('Master Clinical Research Associate competencies and GCP protocols');
  const [aiDurationWeeks, setAiDurationWeeks] = useState(6);
  const [aiSlideCount, setAiSlideCount] = useState(6);
  const [aiTargetCareer, setAiTargetCareer] = useState('Clinical Research Associate (CRA)');
  const [aiHoursPerWeek, setAiHoursPerWeek] = useState(12);
  const [aiConcept, setAiConcept] = useState('Randomized Controlled Trial (RCT)');
  const [aiExplainDepth, setAiExplainDepth] = useState('Intermediate');
  const [aiResult, setAiResult] = useState<any>(null);

  // Manual Note Creator Form
  const [manualNoteForm, setManualNoteForm] = useState({
    title: '',
    topic: '',
    category: 'Clinical Knowledge',
    level: 'Intermediate',
    content: '',
    tags: 'Clinical, Study',
    isAi: false
  });
  const [selectedNote, setSelectedNote] = useState<any>(null);

  // New Post Form
  const [showNewPostModal, setShowNewPostModal] = useState(false);
  const [postForm, setPostForm] = useState({
    title: '',
    content: '',
    category: 'CLINICAL_INSIGHT',
    tags: 'Healthcare, Clinical'
  });

  // Mentorship Request Modal
  const [selectedMentor, setSelectedMentor] = useState<any>(null);
  const [mentorshipForm, setMentorshipForm] = useState({
    subject: 'Career & Research Guidance',
    goals: 'Seeking advice on transitioning into Clinical Research Associate roles and GCP certification.',
    message: 'Hello, I have reviewed your background and would appreciate guidance on study methodology and next career steps.'
  });

  // Opportunity Application Modal
  const [selectedOppForApply, setSelectedOppForApply] = useState<any>(null);
  const [applyCoverNote, setApplyCoverNote] = useState('I am eager to contribute my clinical background and existing coursework skills to this opportunity.');

  // Recruiter New Opportunity Modal
  const [showNewOppModal, setShowNewOppModal] = useState(false);
  const [newOppForm, setNewOppForm] = useState({
    title: '',
    organization: '',
    type: 'INTERNSHIP',
    description: '',
    eligibilitySummary: '',
    requiredQualification: 'BSc Nursing, MBBS, BPharm',
    requiredSpecialization: 'Healthcare',
    requiredSkills: 'Clinical Trials, Medical Terminology, Patient Safety',
    location: 'Bengaluru',
    workMode: 'HYBRID',
    duration: '6 months',
    stipendOrSalary: '₹25,000 / month',
    deadline: '2026-12-31',
    officialUrl: 'https://example.com/careers'
  });

  // Career & Opportunities Tab
  const [careerTab, setCareerTab] = useState<'paths' | 'readiness' | 'jobs' | 'applications'>('paths');

  // Check login state on initial mount
  useEffect(() => {
    async function initAuth() {
      const token = API.getToken();
      if (token) {
        try {
          const u = await API.getCurrentUser();
          setUser(u);
          if (u.role === 'STUDENT' && !u.onboardingCompleted) {
            setShowOnboarding(true);
          }
        } catch {
          API.setToken(null);
          setUser(null);
        }
      }
      setLoading(false);
    }
    initAuth();
  }, []);

  // Fetch data on page or user change
  useEffect(() => {
    if (!user) return;

    if (page === 'Home' || page === 'Dashboard') {
      loadDashboard();
    } else if (page === 'Network') {
      loadNetwork();
    } else if (page === 'Career & Opportunities') {
      loadCareerOpportunities();
    } else if (page === 'Explore') {
      loadExploreData();
    } else if (page === 'AI Learning Studio' || page === 'My Content') {
      loadContentData();
    } else if (page === 'Profile') {
      loadProfileData();
    } else if (page === 'Audit Logs') {
      API.getAuditLogs().then(setAuditLogs).catch(e => setError(e.message));
    }
  }, [page, user, oppTypeFilter, feedCategoryFilter]);

  async function loadDashboard() {
    try {
      if (user?.role === 'STUDENT') {
        const d = await API.api<any>('dashboard/student');
        setDashboardData(d);
        if (d.recommendations) setCareerRecommendations(d.recommendations);
        if (d.readiness) setMarketReadiness(d.readiness);
      } else if (user?.role === 'MENTOR') {
        const d = await API.api<any>('dashboard/mentor');
        setDashboardData(d);
        if (d.pendingRequests) setMentorshipRequests(d.pendingRequests);
      } else if (user?.role === 'RECRUITER') {
        const d = await API.api<any>('dashboard/recruiter');
        setDashboardData(d);
      } else if (user?.role === 'ADMIN') {
        const d = await API.api<any>('dashboard/admin');
        setDashboardData(d);
        if (d.auditLogs) setAuditLogs(d.auditLogs);
      }
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function loadNetwork() {
    try {
      const [f, c, m, mr] = await Promise.all([
        API.getNetworkFeed(feedCategoryFilter),
        API.getConnections(),
        API.getDiscoverableMembers(memberSearchQuery, memberRoleFilter),
        API.getMentorshipRequests(user?.role === 'MENTOR' || user?.role === 'HEALTHCARE_PROFESSIONAL')
      ]);
      setNetworkFeed(f);
      setConnections(c);
      setMembers(m);
      setMentorshipRequests(mr);
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function loadCareerOpportunities() {
    try {
      const [cr, mr, opps, apps] = await Promise.all([
        API.getCareerRecommendations(),
        API.getMarketReadiness(),
        API.getOpportunities(oppTypeFilter),
        API.getMyApplications()
      ]);
      setCareerRecommendations(cr);
      setMarketReadiness(mr);
      setOpportunities(opps);
      setMyApplications(apps);
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function loadExploreData() {
    try {
      const [s, c, cr] = await Promise.all([
        API.getSkills(),
        API.getCareers(),
        API.api<any[]>('courses')
      ]);
      setSkillsCatalog(s);
      setCareersCatalog(c);
      setCourses(cr);
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function loadContentData() {
    try {
      const [n, r, sp] = await Promise.all([
        API.getUserNotes(),
        API.getUserRoadmaps(),
        API.getUserStudyPlans()
      ]);
      setUserNotes(n);
      setUserRoadmaps(r);
      setUserStudyPlans(sp);
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function loadProfileData() {
    try {
      const us = await API.getUserSkills();
      setUserSkills(us);
    } catch (e: any) {
      setError(e.message);
    }
  }

  // Authentication Handlers
  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const res = await API.login({ email, password });
      setUser(res.user);
      setPage(res.user.role === 'ADMIN' ? 'Dashboard' : 'Home');
      if (res.user.role === 'STUDENT' && !res.user.onboardingCompleted) {
        setShowOnboarding(true);
      }
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleSignup(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    if (regForm.password !== regForm.confirm) {
      setError('Passwords do not match.');
      return;
    }
    setBusy(true);
    try {
      const res = await API.register({
        name: regForm.name,
        email: regForm.email,
        password: regForm.password,
        role: regForm.role,
        specialization: regForm.specialization,
        experienceLevel: regForm.experienceLevel,
        location: regForm.location
      });
      setUser(res.user);
      setPage(res.user.role === 'ADMIN' ? 'Dashboard' : 'Home');
      if (res.user.role === 'STUDENT') {
        setShowOnboarding(true);
      }
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  function handleLogout() {
    API.logout();
    setUser(null);
    setDashboardData(null);
    setPage('Home');
    setNotice('You have been logged out securely.');
  }

  // Onboarding Submission
  async function submitOnboardingWizard() {
    setBusy(true);
    setError('');
    try {
      const res = await API.submitOnboarding(onboardingData);
      setUser(res.user);
      setShowOnboarding(false);
      setNotice('Onboarding completed! Your healthcare skill and career profile has been calibrated.');
      setCareerRecommendations(res.recommendations || []);
      setMarketReadiness(res.readiness || null);
      setPage('Career & Opportunities');
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  // Networking Actions
  async function handleCreatePost(e: React.FormEvent) {
    e.preventDefault();
    if (!postForm.content.trim()) return;
    setBusy(true);
    try {
      const tags = postForm.tags.split(',').map(s => s.trim()).filter(Boolean);
      await API.createPost({
        title: postForm.title,
        content: postForm.content,
        category: postForm.category,
        tags
      });
      setShowNewPostModal(false);
      setPostForm({ title: '', content: '', category: 'CLINICAL_INSIGHT', tags: 'Healthcare, Clinical' });
      setNotice('Healthcare post shared with your professional network.');
      loadNetwork();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleReactPost(postId: number) {
    try {
      const res = await API.reactPost(postId);
      setNetworkFeed(prev => prev.map(p => p.id === postId ? { ...p, likedByMe: res.liked, likesCount: res.likesCount } : p));
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function handleAddComment(postId: number, content: string) {
    if (!content.trim()) return;
    try {
      const newComment = await API.addComment(postId, content);
      setNetworkFeed(prev => prev.map(p => {
        if (p.id === postId) {
          return {
            ...p,
            commentsCount: p.commentsCount + 1,
            comments: [...(p.comments || []), newComment]
          };
        }
        return p;
      }));
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function handleConnect(recipientId: string) {
    try {
      await API.sendConnectionRequest(recipientId);
      setNotice('Connection request sent.');
      loadNetwork();
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function handleUpdateConnection(connectionId: number, action: 'ACCEPT' | 'DECLINE') {
    try {
      await API.updateConnection(connectionId, action);
      setNotice(`Connection request ${action.toLowerCase()}ed.`);
      loadNetwork();
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function handleSubmitMentorshipRequest(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedMentor) return;
    setBusy(true);
    try {
      await API.requestMentorship({
        mentorId: selectedMentor.id,
        subject: mentorshipForm.subject,
        goals: mentorshipForm.goals,
        message: mentorshipForm.message
      });
      setSelectedMentor(null);
      setNotice('Mentorship request sent to ' + selectedMentor.fullName + '.');
      loadNetwork();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  // Opportunity Actions
  async function handleToggleSaveOpportunity(oppId: string) {
    try {
      const res = await API.toggleSaveOpportunity(oppId);
      setOpportunities(prev => prev.map(o => o.id === oppId ? { ...o, isSaved: res.saved } : o));
      setNotice(res.saved ? 'Opportunity saved to your profile.' : 'Opportunity removed from saved.');
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function handleApplyOpportunity(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedOppForApply) return;
    setBusy(true);
    try {
      await API.applyOpportunity(selectedOppForApply.id, { coverNote: applyCoverNote });
      setSelectedOppForApply(null);
      setNotice('Application submitted successfully to ' + selectedOppForApply.organization + '.');
      loadCareerOpportunities();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handlePostOpportunity(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    try {
      const skills = newOppForm.requiredSkills.split(',').map(s => s.trim()).filter(Boolean);
      await API.postOpportunity({
        ...newOppForm,
        requiredSkills: skills
      });
      setShowNewOppModal(false);
      setNotice('Healthcare opportunity published.');
      loadCareerOpportunities();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  // AI Actions
  async function handleRunAiAction(actionType: string) {
    setBusy(true);
    setError('');
    setNotice('');
    try {
      if (actionType === 'notes') {
        const res = await API.generateNotes({ topic: aiTopic, level: aiLevel });
        setAiResult(res);
      } else if (actionType === 'roadmap') {
        const res = await API.generateRoadmap({
          goal: aiGoal,
          specialization: user?.specialization || 'Healthcare',
          duration_weeks: aiDurationWeeks
        });
        setAiResult(res);
      } else if (actionType === 'presentation') {
        const blob = await API.generatePresentation({
          topic: aiTopic,
          title: aiTopic + ' Clinical Study Presentation',
          audience: 'Healthcare learners & clinical residents',
          slide_count: aiSlideCount
        });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `healthskill-${aiTopic.toLowerCase().replace(/\\s+/g, '-')}.pptx`;
        a.click();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
        setNotice('PowerPoint .pptx file generated and downloaded successfully.');
      } else if (actionType === 'study_plan') {
        const res = await API.generateStudyPlan({
          target_career: aiTargetCareer,
          hours_per_week: aiHoursPerWeek,
          current_level: aiLevel
        });
        setAiResult(res);
      } else if (actionType === 'explain') {
        const res = await API.explainConcept({
          concept: aiConcept,
          depth: aiExplainDepth
        });
        setAiResult(res);
      } else if (actionType === 'skill_plan') {
        const missing = marketReadiness?.recommendedNextSkills || ['Clinical Trials', 'Electronic Health Records (EHR)'];
        const res = await API.generateSkillPlan({
          target_career: aiTargetCareer,
          missing_skills: missing
        });
        setAiResult(res);
      }
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleSaveGeneratedNote() {
    if (!aiResult) return;
    setBusy(true);
    try {
      await API.createNote({
        title: `${aiResult.topic || aiTopic} - AI Study Aid`,
        topic: aiResult.topic || aiTopic,
        category: 'Clinical Knowledge',
        level: aiLevel,
        content: `Generated study aid for ${aiResult.topic || aiTopic}.`,
        sections: aiResult.sections || [],
        selfTest: aiResult.selfTest || [],
        tags: [aiResult.topic || aiTopic, 'AI Study Aid'],
        isAiGenerated: true,
        visibility: 'PRIVATE'
      });
      setNotice('Note saved to My Notes library in MySQL.');
      loadContentData();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleSaveManualNote(e: React.FormEvent) {
    e.preventDefault();
    if (!manualNoteForm.title.trim() || !manualNoteForm.content.trim()) return;
    setBusy(true);
    try {
      const tags = manualNoteForm.tags.split(',').map(s => s.trim()).filter(Boolean);
      await API.createNote({
        title: manualNoteForm.title,
        topic: manualNoteForm.topic || manualNoteForm.title,
        category: manualNoteForm.category,
        level: manualNoteForm.level,
        content: manualNoteForm.content,
        tags,
        isAiGenerated: false,
        visibility: 'PRIVATE'
      });
      setManualNoteForm({
        title: '',
        topic: '',
        category: 'Clinical Knowledge',
        level: 'Intermediate',
        content: '',
        tags: 'Clinical, Study',
        isAi: false
      });
      setNotice('Manual study note saved to your MySQL database.');
      loadContentData();
      setAiStudioTab('notes');
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleDeleteNote(id: number) {
    try {
      await API.deleteNote(id);
      setUserNotes(prev => prev.filter(n => n.id !== id));
      if (selectedNote?.id === id) setSelectedNote(null);
      setNotice('Note deleted.');
    } catch (e: any) {
      setError(e.message);
    }
  }

  // User Skill Management
  async function handleAddSkill(name: string, proficiency: string) {
    try {
      const s = await API.addUserSkill({ name, proficiency, evidenceType: 'SELF_REPORTED' });
      setUserSkills(prev => [...prev, s]);
      setNotice(`Skill "${name}" added to your profile.`);
      loadProfileData();
    } catch (e: any) {
      setError(e.message);
    }
  }

  async function handleDeleteSkill(id: number) {
    try {
      await API.deleteUserSkill(id);
      setUserSkills(prev => prev.filter(s => s.id !== id));
      setNotice('Skill removed.');
    } catch (e: any) {
      setError(e.message);
    }
  }

  // PubMed Research Search
  const [researchQuery, setResearchQuery] = useState('');
  const [researchResults, setResearchResults] = useState<any>(null);
  async function handleSearchResearch() {
    if (researchQuery.trim().length < 2) return;
    setBusy(true);
    setError('');
    try {
      const res = await API.searchResearch(researchQuery);
      setResearchResults(res);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  // Loading Screen
  if (loading) {
    return (
      <div className="fullscreen-loader">
        <div className="loader-box">
          <HeartPulse size={48} className="pulse" />
          <h2>HealthSkill Connect</h2>
          <p className="muted">Initializing healthcare ecosystem...</p>
        </div>
      </div>
    );
  }

  // 1. PUBLIC LANDING & AUTH LAYOUT (if not logged in)
  if (!user) {
    return (
      <div className="public-landing">
        <header className="public-nav">
          <div className="brand">
            <span className="brand-mark"><HeartPulse /></span>
            <span>HealthSkill <b>Connect</b></span>
          </div>
          <div className="nav-links">
            <a href="#pillars">Ecosystem Pillars</a>
            <a href="#careers">Career Discovery</a>
            <a href="#networking">Networking</a>
            <a href="#ai-studio">AI Studio</a>
          </div>
          <div className="nav-cta">
            <button className="outline" onClick={() => { setAuthMode('login'); setError(''); }}>Sign In</button>
            <button className="primary" onClick={() => { setAuthMode('signup'); setError(''); }}>Get Started <ChevronRight size={16} /></button>
          </div>
        </header>

        {/* Hero Section */}
        <section className="landing-hero">
          <div className="hero-badge">
            <Sparkles size={14} /> HEALTHCARE PROFESSIONAL & CAREER ECOSYSTEM
          </div>
          <h1>
            Connect. Discover. Master.<br />
            <span>The Healthcare Career Platform.</span>
          </h1>
          <p className="hero-sub">
            Bridging professional networking, career intelligence, skill matching, verified jobs, and AI learning into one dedicated healthcare ecosystem.
          </p>

          <div className="hero-actions">
            <button className="primary large" onClick={() => { setAuthMode('signup'); setError(''); }}>
              Create Learner Account <ChevronRight size={18} />
            </button>
            <a href="#demo-access" className="outline large">
              Explore Demo Accounts <ArrowUpRight size={18} />
            </a>
          </div>

          <div className="hero-stats-banner">
            <div><strong>5 Pillars</strong><span>Unified Ecosystem</span></div>
            <div><strong>20+</strong><span>Clinical & Tech Skills</span></div>
            <div><strong>7+</strong><span>Career Pathways</span></div>
            <div><strong>MySQL 8.0</strong><span>Verified Persistence</span></div>
          </div>
        </section>

        {/* Auth Section with Live Demo Switcher */}
        <section className="auth-dock-section" id="demo-access">
          <div className="auth-card-modern">
            <div className="auth-tabs">
              <button className={authMode === 'login' ? 'active' : ''} onClick={() => { setAuthMode('login'); setError(''); }}>Sign In</button>
              <button className={authMode === 'signup' ? 'active' : ''} onClick={() => { setAuthMode('signup'); setError(''); }}>Register New User</button>
            </div>

            {error && <div className="alert error">{error}</div>}
            {notice && <div className="alert success">{notice}</div>}

            {authMode === 'login' ? (
              <form onSubmit={handleLogin} className="form-stack">
                <div className="demo-accounts-picker">
                  <span className="picker-label"><ShieldCheck size={14} /> Quick Demo Account Switcher</span>
                  <div className="demo-chips">
                    {DEMO_ACCOUNTS.map(d => (
                      <button
                        type="button"
                        key={d.label}
                        className={email === d.email ? 'chip active' : 'chip'}
                        onClick={() => { setEmail(d.email); setPassword(DEMO_PASSWORD); }}
                      >
                        {d.label}
                      </button>
                    ))}
                  </div>
                </div>

                <label>
                  Email Address
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    placeholder="you@example.com"
                  />
                </label>
                <label>
                  Password
                  <div className="password-wrap">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      required
                      value={password}
                      onChange={e => setPassword(e.target.value)}
                    />
                    <button
                      type="button"
                      className="icon-btn"
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </label>
                <button className="primary full" disabled={busy}>
                  {busy ? <LoaderCircle className="spin" size={18} /> : <LockKeyhole size={18} />}
                  Sign In to HealthSkill Connect
                </button>
              </form>
            ) : (
              <form onSubmit={handleSignup} className="form-stack">
                <div className="two-col">
                  <label>
                    Full Name
                    <input
                      required
                      value={regForm.name}
                      onChange={e => setRegForm({ ...regForm, name: e.target.value })}
                      placeholder="Dr. / Nurse / Student Name"
                    />
                  </label>
                  <label>
                    Email Address
                    <input
                      type="email"
                      required
                      value={regForm.email}
                      onChange={e => setRegForm({ ...regForm, email: e.target.value })}
                      placeholder="you@example.com"
                    />
                  </label>
                </div>

                <div className="two-col">
                  <label>
                    Account Role
                    <select
                      value={regForm.role}
                      onChange={e => setRegForm({ ...regForm, role: e.target.value })}
                    >
                      <option value="STUDENT">Healthcare Student</option>
                      <option value="MENTOR">Educator / Mentor</option>
                      <option value="HEALTHCARE_PROFESSIONAL">Healthcare Professional</option>
                      <option value="RECRUITER">Recruiter / Organization</option>
                    </select>
                  </label>
                  <label>
                    Specialization
                    <input
                      required
                      value={regForm.specialization}
                      onChange={e => setRegForm({ ...regForm, specialization: e.target.value })}
                      placeholder="Nursing, MBBS, Pharmacy, Lab Tech"
                    />
                  </label>
                </div>

                <div className="two-col">
                  <label>
                    Year / Experience Level
                    <select
                      value={regForm.experienceLevel}
                      onChange={e => setRegForm({ ...regForm, experienceLevel: e.target.value })}
                    >
                      <option>Year 1</option>
                      <option>Year 2</option>
                      <option>Year 3</option>
                      <option>Year 4</option>
                      <option>Year 5</option>
                      <option>1-3 years experience</option>
                      <option>4-7 years experience</option>
                      <option>8+ years experience</option>
                    </select>
                  </label>
                  <label>
                    Location
                    <input
                      required
                      value={regForm.location}
                      onChange={e => setRegForm({ ...regForm, location: e.target.value })}
                      placeholder="Delhi, Mumbai, Bengaluru..."
                    />
                  </label>
                </div>

                <div className="two-col">
                  <label>
                    Password
                    <input
                      type="password"
                      required
                      value={regForm.password}
                      onChange={e => setRegForm({ ...regForm, password: e.target.value })}
                    />
                  </label>
                  <label>
                    Confirm Password
                    <input
                      type="password"
                      required
                      value={regForm.confirm}
                      onChange={e => setRegForm({ ...regForm, confirm: e.target.value })}
                    />
                  </label>
                </div>
                <small className="muted">8+ characters with uppercase, lowercase, number, and special character.</small>
                <button className="primary full" disabled={busy}>
                  {busy ? <LoaderCircle className="spin" size={18} /> : <UserRound size={18} />}
                  Create Account & Begin Career Discovery
                </button>
              </form>
            )}
          </div>
        </section>

        {/* 5 Pillars Section */}
        <section className="pillars-section" id="pillars">
          <div className="section-title text-center">
            <span className="eyebrow">THE 5 PILLARS</span>
            <h2>A Complete Healthcare Ecosystem</h2>
            <p className="muted">Connecting learning, career discovery, networking, and opportunities seamlessly.</p>
          </div>

          <div className="pillars-grid">
            <div className="pillar-card">
              <span className="pillar-icon teal"><Users size={24} /></span>
              <h3>1. Professional Networking</h3>
              <p>Connect with healthcare learners, clinical professors, researchers, and accredited hospitals. Share clinical insights, case discussions, and seek mentor guidance.</p>
            </div>
            <div className="pillar-card">
              <span className="pillar-icon blue"><Compass size={24} /></span>
              <h3>2. Career Discovery & Intelligence</h3>
              <p>Explore high-growth healthcare career paths (CRA, Data Analyst, Pharmacovigilance, Epidemiologist) with required qualifications, certifications, and realistic salary bands.</p>
            </div>
            <div className="pillar-card">
              <span className="pillar-icon purple"><Award size={24} /></span>
              <h3>3. Skill Intelligence & Matching</h3>
              <p>Evaluate your skills against industry standards. Identify missing competencies, upload project/cert evidence, and receive transparent matching scores.</p>
            </div>
            <div className="pillar-card">
              <span className="pillar-icon orange"><BriefcaseMedical size={24} /></span>
              <h3>4. Jobs & Internships</h3>
              <p>Discover verified healthcare internships, clinical residencies, and research fellowships. Direct recruiter publishing and application tracking in MySQL.</p>
            </div>
            <div className="pillar-card">
              <span className="pillar-icon green"><Brain size={24} /></span>
              <h3>5. AI Learning Studio</h3>
              <p>Create manual or AI-generated study notes, personalized learning roadmaps, real downloadable PowerPoint decks (.pptx), study schedules, and PubMed literature discovery.</p>
            </div>
          </div>
        </section>

        <footer className="public-footer">
          <div>
            <HeartPulse size={16} /> HealthSkill Connect © 2026. Evidence-first healthcare career & learning ecosystem.
          </div>
          <div>MySQL 8.0 Persistence • Spring Boot • Python AI • PubMed E-utilities</div>
        </footer>
      </div>
    );
  }

  // Navigation Items per Role
  const role = user.role;
  let navItems: { name: string; icon: any }[] = [];
  if (role === 'STUDENT') {
    navItems = [
      { name: 'Home', icon: Activity },
      { name: 'Network', icon: Users },
      { name: 'Explore', icon: Compass },
      { name: 'Career & Opportunities', icon: BriefcaseMedical },
      { name: 'AI Learning Studio', icon: Brain },
      { name: 'Profile', icon: UserRound }
    ];
  } else if (role === 'MENTOR') {
    navItems = [
      { name: 'Dashboard', icon: Activity },
      { name: 'Network', icon: Users },
      { name: 'My Content', icon: FileText },
      { name: 'AI Learning Studio', icon: Brain },
      { name: 'Classes', icon: GraduationCap },
      { name: 'Profile', icon: UserRound }
    ];
  } else if (role === 'HEALTHCARE_PROFESSIONAL') {
    navItems = [
      { name: 'Home', icon: Activity },
      { name: 'Network', icon: Users },
      { name: 'Opportunities', icon: BriefcaseMedical },
      { name: 'Content', icon: FileText },
      { name: 'Profile', icon: UserRound }
    ];
  } else if (role === 'RECRUITER') {
    navItems = [
      { name: 'Dashboard', icon: Activity },
      { name: 'Opportunities', icon: BriefcaseMedical },
      { name: 'Candidates', icon: Users },
      { name: 'Profile', icon: Building2 }
    ];
  } else { // ADMIN
    navItems = [
      { name: 'Dashboard', icon: Activity },
      { name: 'Explore', icon: Compass },
      { name: 'Audit Logs', icon: ShieldCheck },
      { name: 'Profile', icon: UserRound }
    ];
  }

  const roleBadgeLabel = role === 'ADMIN' ? 'Platform Administrator'
    : role === 'MENTOR' ? 'Healthcare Educator / Mentor'
    : role === 'HEALTHCARE_PROFESSIONAL' ? 'Healthcare Professional'
    : role === 'RECRUITER' ? 'Healthcare Recruiter / Org'
    : 'Healthcare Student';

  return (
    <div className="app-shell">
      {/* Sidebar Navigation */}
      <aside className={`sidebar ${mobileMenuOpen ? 'open' : ''}`}>
        <div className="brand sidebar-brand">
          <span className="brand-mark"><HeartPulse /></span>
          <span>HealthSkill <b>Connect</b></span>
          <button className="close-mobile icon-btn" onClick={() => setMobileMenuOpen(false)}>
            <X size={18} />
          </button>
        </div>

        <div className="workspace-label">HEALTHCARE WORKSPACE</div>
        <nav>
          {navItems.map(item => (
            <button
              key={item.name}
              className={`nav-item ${page === item.name ? 'active' : ''}`}
              onClick={() => {
                setPage(item.name);
                setAiResult(null);
                setMobileMenuOpen(false);
              }}
            >
              <item.icon size={19} />
              <span>{item.name}</span>
              {page === item.name && <span className="nav-indicator" />}
            </button>
          ))}
        </nav>

        <div className="sidebar-bottom">
          {role === 'STUDENT' && (
            <div className="support-card">
              <div className="support-icon"><Sparkles size={18} /></div>
              <strong>Career Discovery</strong>
              <p>Re-calibrate your interests anytime.</p>
              <button onClick={() => setShowOnboarding(true)}>
                Career Questionnaire <ChevronRight size={14} />
              </button>
            </div>
          )}

          <button className="nav-item logout" onClick={handleLogout}>
            <LogOut size={18} />
            <span>Sign out</span>
          </button>
        </div>
      </aside>

      {mobileMenuOpen && (
        <button
          className="scrim"
          aria-label="Close menu"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}

      {/* Main Content Area */}
      <main className="main-area">
        {/* Top Header Bar */}
        <header className="topbar">
          <button
            className="mobile-menu icon-btn"
            onClick={() => setMobileMenuOpen(true)}
            aria-label="Open navigation"
          >
            <Menu size={20} />
          </button>

          <div className="breadcrumbs">
            <span>Workspace</span>
            <ChevronRight size={14} />
            <strong>{page}</strong>
          </div>

          <div className="top-actions">
            <span className="secure-label">
              <ShieldCheck size={16} /> MySQL Persistence Active
            </span>
            <button className="profile-chip" onClick={() => setPage('Profile')}>
              <span className="avatar">{user.avatarInitials || 'HC'}</span>
              <span className="profile-chip-text">
                <strong>{user.fullName}</strong>
                <small>{user.specialization || roleBadgeLabel}</small>
              </span>
              <ChevronRight size={15} />
            </button>
          </div>
        </header>

        {/* Dynamic Alerts */}
        <div className="page-content">
          {error && (
            <div className="alert error page-alert">
              <span><AlertCircle size={16} /> {error}</span>
              <button onClick={() => setError('')}>Dismiss</button>
            </div>
          )}
          {notice && (
            <div className="alert success page-alert">
              <span><CheckCircle2 size={16} /> {notice}</span>
              <button onClick={() => setNotice('')}>Dismiss</button>
            </div>
          )}

          {/* ============================================================== */}
          {/* VIEW: HOME / DASHBOARD (ROLE-TAILORED) */}
          {/* ============================================================== */}
          {(page === 'Home' || page === 'Dashboard') && (
            <>
              {/* STUDENT HOME */}
              {role === 'STUDENT' && (
                <>
                  <div className="hero-card">
                    <div className="hero-content">
                      <span className="hero-tag">
                        <CheckCircle2 size={14} /> CAREER INTELLIGENCE DASHBOARD
                      </span>
                      <h2>
                        Good day, {user.fullName.split(' ')[0]}!<br />
                        Shape your next healthcare milestone.
                      </h2>
                      <p>
                        Track industry readiness in {user.specialization || 'Healthcare'}, close skill gaps, and connect with clinical mentors.
                      </p>
                      <div className="hero-btns">
                        <button className="light-button" onClick={() => { setPage('Career & Opportunities'); setCareerTab('paths'); }}>
                          Explore Career Paths <ChevronRight size={16} />
                        </button>
                        <button className="outline-light-button" onClick={() => setShowOnboarding(true)}>
                          Update Questionnaire
                        </button>
                      </div>
                    </div>
                    <div className="hero-visual">
                      <div className="orbit orbit-one" />
                      <div className="orbit orbit-two" />
                      <div className="hero-circle"><HeartPulse size={56} /></div>
                      <div className="float-card float-one">
                        <Award size={17} />
                        <span><b>Readiness: {marketReadiness?.readinessTier || 'Developing'}</b><small>{marketReadiness?.readinessScore || 65}% match score</small></span>
                      </div>
                      <div className="float-card float-two">
                        <Compass size={17} />
                        <span><b>{careerRecommendations.length} Careers</b><small>Mapped to your skills</small></span>
                      </div>
                    </div>
                  </div>

                  {/* Metrics Row */}
                  <div className="metric-grid">
                    <Metric icon={TrendingUp} label="Market Readiness" value={`${marketReadiness?.readinessScore || 65}%`} tone="teal" />
                    <Metric icon={Compass} label="Matched Careers" value={careerRecommendations.length || 7} tone="blue" />
                    <Metric icon={BriefcaseMedical} label="Opportunities" value={opportunities.length || 5} tone="orange" />
                    <Metric icon={FileText} label="Saved Notes" value={userNotes.length || 1} tone="purple" />
                  </div>

                  {/* Split Content: Recommended Careers & Quick Actions */}
                  <div className="content-grid">
                    <section className="panel">
                      <div className="section-heading">
                        <div>
                          <h3>Top Matched Healthcare Careers</h3>
                          <p>Explainable recommendations based on your qualifications and skills</p>
                        </div>
                        <button className="text-link" onClick={() => { setPage('Career & Opportunities'); setCareerTab('paths'); }}>
                          View all <ChevronRight size={14} />
                        </button>
                      </div>

                      <div className="recs-list">
                        {careerRecommendations.slice(0, 3).map((c: any) => (
                          <div className="rec-card" key={c.careerId}>
                            <div className="rec-top">
                              <div>
                                <h4>{c.careerName}</h4>
                                <span className="soft-tag">{c.category}</span>
                              </div>
                              <span className="match-pill">{c.matchPercentage}% Match</span>
                            </div>
                            <p className="rec-explanation">{c.explanation}</p>
                            <div className="rec-skills-preview">
                              <small>Possessed Skills: <b>{c.matchedSkills?.join(', ') || 'Domain fundamentals'}</b></small>
                              {c.missingSkills?.length > 0 && (
                                <small className="text-orange">Next to learn: <b>{c.missingSkills.slice(0, 2).join(', ')}</b></small>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    </section>

                    <section className="panel">
                      <div className="section-heading">
                        <div>
                          <h3>Ecosystem Quick Actions</h3>
                          <p>Jump directly to high-impact workflows</p>
                        </div>
                      </div>
                      <div className="quick-grid">
                        <Quick icon={Brain} label="Generate Study Notes" onClick={() => { setPage('AI Learning Studio'); setAiStudioTab('builder'); }} />
                        <Quick icon={Presentation} label="PowerPoint Maker" onClick={() => { setPage('AI Learning Studio'); setAiStudioTab('presentation'); }} />
                        <Quick icon={BriefcaseMedical} label="Explore Internships" onClick={() => { setPage('Career & Opportunities'); setCareerTab('jobs'); }} />
                        <Quick icon={Users} label="Healthcare Feed" onClick={() => setPage('Network')} />
                        <Quick icon={Microscope} label="PubMed Research" onClick={() => setPage('Explore')} />
                        <Quick icon={SlidersHorizontal} label="Update Questionnaire" onClick={() => setShowOnboarding(true)} />
                      </div>

                      {/* Market in-demand snapshot */}
                      {marketReadiness?.inDemandSkills?.length > 0 && (
                        <div className="demand-snapshot">
                          <h4><TrendingUp size={15} /> Skills Currently in Demand</h4>
                          <div className="skill-chip-wrap">
                            {marketReadiness.inDemandSkills.slice(0, 5).map((s: any) => (
                              <span key={s.skill} className={`skill-chip ${s.possessed ? 'possessed' : 'needed'}`}>
                                {s.skill} {s.possessed ? '✓' : `(${s.demandCount} jobs)`}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}
                    </section>
                  </div>
                </>
              )}

              {/* MENTOR / EDUCATOR DASHBOARD */}
              {role === 'MENTOR' && (
                <>
                  <div className="welcome-row">
                    <div>
                      <span className="eyebrow">EDUCATOR & MENTOR WORKSPACE</span>
                      <h1>Welcome, {user.fullName}!</h1>
                      <p className="muted">Guide healthcare students, review mentorship requests, and manage educational resources.</p>
                    </div>
                  </div>

                  <div className="metric-grid">
                    <Metric icon={Users} label="Mentorship Inquiries" value={mentorshipRequests.length} tone="teal" />
                    <Metric icon={FileText} label="Authored Notes" value={userNotes.length} tone="blue" />
                    <Metric icon={GraduationCap} label="Classes Active" value={2} tone="purple" />
                    <Metric icon={Award} label="Credentials" value="Verified" tone="orange" />
                  </div>

                  <div className="content-grid">
                    <section className="panel">
                      <div className="section-heading">
                        <div>
                          <h3>Student Mentorship Requests</h3>
                          <p>Healthcare learners seeking career and academic guidance</p>
                        </div>
                      </div>

                      {mentorshipRequests.length === 0 ? (
                        <div className="empty-state">
                          <Users size={32} />
                          <strong>No pending mentorship inquiries</strong>
                          <p>Requests from healthcare students will appear here.</p>
                        </div>
                      ) : (
                        <div className="list-stack">
                          {mentorshipRequests.map((mr: any) => (
                            <div className="mentorship-card" key={mr.id}>
                              <div className="mr-top">
                                <span className="avatar small">{mr.studentAvatar || 'ST'}</span>
                                <div>
                                  <strong>{mr.studentName}</strong>
                                  <small>{mr.studentSpecialization} · {new Date(mr.createdAt).toLocaleDateString()}</small>
                                </div>
                                <span className={`status-pill ${mr.status.toLowerCase()}`}>{mr.status}</span>
                              </div>
                              <p><b>Subject:</b> {mr.subject}</p>
                              <p className="muted">{mr.message}</p>
                              {mr.status === 'PENDING' && (
                                <div className="btn-row">
                                  <button className="primary small" onClick={() => API.updateMentorshipRequest(mr.id, 'ACCEPTED').then(() => { setNotice('Mentorship request accepted.'); loadDashboard(); })}>
                                    Accept Request
                                  </button>
                                  <button className="outline small" onClick={() => API.updateMentorshipRequest(mr.id, 'DECLINED').then(() => { setNotice('Mentorship request declined.'); loadDashboard(); })}>
                                    Decline
                                  </button>
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                    </section>

                    <section className="panel">
                      <div className="section-heading">
                        <div>
                          <h3>Quick Teaching Tools</h3>
                          <p>Create content for your classes and mentees</p>
                        </div>
                      </div>
                      <div className="quick-grid">
                        <Quick icon={FileText} label="Create Study Notes" onClick={() => { setPage('AI Learning Studio'); setAiStudioTab('builder'); }} />
                        <Quick icon={Presentation} label="Generate Slides" onClick={() => { setPage('AI Learning Studio'); setAiStudioTab('presentation'); }} />
                        <Quick icon={Compass} label="Build Syllabus Roadmap" onClick={() => { setPage('AI Learning Studio'); setAiStudioTab('roadmap'); }} />
                        <Quick icon={Users} label="Share in Network Feed" onClick={() => setPage('Network')} />
                      </div>
                    </section>
                  </div>
                </>
              )}

              {/* HEALTHCARE PROFESSIONAL HOME */}
              {role === 'HEALTHCARE_PROFESSIONAL' && (
                <>
                  <div className="welcome-row">
                    <div>
                      <span className="eyebrow">CLINICAL & RESEARCH NETWORK</span>
                      <h1>Dr. {user.fullName.replace(/^Dr\.\s*/i, '')}</h1>
                      <p className="muted">Connect with peers, review clinical research discussions, and mentor the next generation.</p>
                    </div>
                  </div>

                  <div className="metric-grid">
                    <Metric icon={Stethoscope} label="Specialty" value={user.specialization || 'Clinical'} tone="teal" />
                    <Metric icon={Users} label="Network Peers" value={connections.length || 4} tone="blue" />
                    <Metric icon={Award} label="Status" value="Verified Clinician" tone="purple" />
                    <Metric icon={FileText} label="Contributions" value={networkFeed.filter(p => p.authorId === user.id).length} tone="orange" />
                  </div>

                  <section className="panel">
                    <div className="section-heading">
                      <div>
                        <h3>Clinical Feed & Peer Updates</h3>
                        <p>Share case discussions, research insights, and professional guidelines</p>
                      </div>
                      <button className="primary" onClick={() => setShowNewPostModal(true)}>
                        <Plus size={16} /> Share Clinical Update
                      </button>
                    </div>

                    <div className="feed-stream">
                      {networkFeed.slice(0, 3).map((post: any) => (
                        <FeedItem
                          key={post.id}
                          post={post}
                          onReact={() => handleReactPost(post.id)}
                          onComment={(c) => handleAddComment(post.id, c)}
                        />
                      ))}
                    </div>
                  </section>
                </>
              )}

              {/* RECRUITER DASHBOARD */}
              {role === 'RECRUITER' && (
                <>
                  <div className="welcome-row">
                    <div>
                      <span className="eyebrow">ORGANIZATION & TALENT WORKSPACE</span>
                      <h1>{user.fullName}</h1>
                      <p className="muted">Post healthcare opportunities, specify required skills, and review student applications.</p>
                    </div>
                    <button className="primary" onClick={() => setShowNewOppModal(true)}>
                      <Plus size={16} /> Post New Opportunity
                    </button>
                  </div>

                  <div className="metric-grid">
                    <Metric icon={BriefcaseMedical} label="Active Postings" value={opportunities.length} tone="teal" />
                    <Metric icon={Users} label="Candidate Pool" value={members.length || 15} tone="blue" />
                    <Metric icon={Building2} label="Institution" value={user.institution || 'Hospital Network'} tone="purple" />
                    <Metric icon={ShieldCheck} label="Verification" value="Authorized" tone="orange" />
                  </div>

                  <section className="panel">
                    <div className="section-heading">
                      <div>
                        <h3>Active Healthcare Postings</h3>
                        <p>Track applications and skill requirements</p>
                      </div>
                    </div>
                    <div className="card-grid">
                      {opportunities.map((opp: any) => (
                        <div className="opportunity-card" key={opp.id}>
                          <div className="opp-top">
                            <span className="opp-icon"><BriefcaseMedical size={20} /></span>
                            <span className="soft-tag">{opp.type}</span>
                          </div>
                          <h3>{opp.title}</h3>
                          <strong>{opp.organization}</strong>
                          <p>{opp.eligibilitySummary}</p>
                          <small className="muted">{opp.location} · {opp.workMode}</small>
                          <div className="opp-skills-tag-wrap">
                            {(opp.requiredSkills || []).map((s: string) => (
                              <span key={s} className="tiny-tag">{s}</span>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  </section>
                </>
              )}

              {/* ADMIN DASHBOARD */}
              {role === 'ADMIN' && (
                <>
                  <div className="welcome-row">
                    <div>
                      <span className="eyebrow">PLATFORM ADMINISTRATION</span>
                      <h1>System Operations & Audit Center</h1>
                      <p className="muted">Monitor user registries, assign roles, and audit security events.</p>
                    </div>
                  </div>

                  <div className="metric-grid">
                    <Metric icon={Users} label="Total Users" value={dashboardData?.counts?.total ?? '7'} tone="teal" />
                    <Metric icon={GraduationCap} label="Students" value={dashboardData?.counts?.students ?? '2'} tone="blue" />
                    <Metric icon={UserRound} label="Mentors" value={dashboardData?.counts?.mentors ?? '2'} tone="purple" />
                    <Metric icon={ShieldCheck} label="Audit Events" value={auditLogs.length || 24} tone="orange" />
                  </div>

                  <section className="panel">
                    <div className="section-heading">
                      <div>
                        <h3>User Registry & Role Management</h3>
                        <p>Persisted in MySQL users table</p>
                      </div>
                    </div>
                    <div className="table-wrap">
                      <table>
                        <thead>
                          <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Specialization</th>
                            <th>Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          {(dashboardData?.users || []).map((u: any) => (
                            <tr key={u.id}>
                              <td>
                                <span className="table-person">
                                  <span className="avatar small">{u.avatarInitials}</span>
                                  {u.fullName}
                                </span>
                              </td>
                              <td>{u.email}</td>
                              <td><span className="role-tag">{u.role}</span></td>
                              <td>{u.specialization || 'N/A'}</td>
                              <td>
                                <select
                                  value={u.role}
                                  onChange={e => API.changeUserRole(u.id, e.target.value).then(() => { setNotice('Role updated.'); loadDashboard(); })}
                                >
                                  <option value="STUDENT">STUDENT</option>
                                  <option value="MENTOR">MENTOR</option>
                                  <option value="HEALTHCARE_PROFESSIONAL">HEALTHCARE_PROFESSIONAL</option>
                                  <option value="RECRUITER">RECRUITER</option>
                                  <option value="ADMIN">ADMIN</option>
                                </select>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </section>
                </>
              )}
            </>
          )}

          {/* ============================================================== */}
          {/* VIEW: NETWORK (PROFESSIONAL HEALTHCARE NETWORKING) */}
          {/* ============================================================== */}
          {page === 'Network' && (
            <div className="network-layout">
              <div className="network-header">
                <div>
                  <PageTitle
                    title="Healthcare Professional Network"
                    subtitle="Connect with verified healthcare practitioners, educators, and peers. Share clinical insights and educational discussions."
                  />
                </div>
                <button className="primary" onClick={() => setShowNewPostModal(true)}>
                  <Plus size={16} /> Create Healthcare Post
                </button>
              </div>

              {/* Feed Filters */}
              <div className="feed-filters">
                {['ALL', 'CLINICAL_INSIGHT', 'CASE_STUDY', 'RESEARCH_UPDATE', 'CAREER_ADVICE'].map(cat => (
                  <button
                    key={cat}
                    className={`filter-btn ${feedCategoryFilter === cat ? 'active' : ''}`}
                    onClick={() => setFeedCategoryFilter(cat)}
                  >
                    {cat.replace(/_/g, ' ')}
                  </button>
                ))}
              </div>

              <div className="network-two-col">
                {/* Left Stream: Posts */}
                <div className="feed-column">
                  {networkFeed.length === 0 ? (
                    <div className="panel empty-state">
                      <MessageSquare size={32} />
                      <strong>No posts in this category yet</strong>
                      <p>Be the first to share a clinical discussion or educational update.</p>
                    </div>
                  ) : (
                    networkFeed.map((post: any) => (
                      <FeedItem
                        key={post.id}
                        post={post}
                        onReact={() => handleReactPost(post.id)}
                        onComment={(c) => handleAddComment(post.id, c)}
                      />
                    ))
                  )}
                </div>

                {/* Right Column: Discover Healthcare Members & Connections */}
                <div className="members-column">
                  <div className="panel">
                    <div className="section-heading">
                      <div>
                        <h3>Discover Healthcare Members</h3>
                        <p>Search by specialty, role, or clinical focus</p>
                      </div>
                    </div>

                    <div className="search-bar-small">
                      <Search size={16} />
                      <input
                        placeholder="Search name, specialty..."
                        value={memberSearchQuery}
                        onChange={e => setMemberSearchQuery(e.target.value)}
                        onKeyDown={e => e.key === 'Enter' && loadNetwork()}
                      />
                    </div>

                    <div className="members-list">
                      {members.slice(0, 6).map((m: any) => (
                        <div className="member-card" key={m.id}>
                          <span className="avatar">{m.avatarInitials}</span>
                          <div className="grow">
                            <strong>{m.fullName}</strong>
                            <small>{m.specialization || m.role} · {m.institution || 'Healthcare'}</small>
                            <span className="tiny-role">{m.role}</span>
                          </div>
                          {m.connectionStatus === 'ACCEPTED' ? (
                            <span className="connected-badge"><Check size={14} /> Connected</span>
                          ) : m.connectionStatus === 'PENDING' ? (
                            <span className="pending-badge">Pending</span>
                          ) : (
                            <div className="member-actions">
                              <button className="outline small" onClick={() => handleConnect(m.id)}>
                                Connect
                              </button>
                              {(m.role === 'MENTOR' || m.role === 'HEALTHCARE_PROFESSIONAL') && (
                                <button
                                  className="primary small icon-only"
                                  title="Request Mentorship"
                                  onClick={() => setSelectedMentor(m)}
                                >
                                  <GraduationCap size={14} />
                                </button>
                              )}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* My Connections List */}
                  <div className="panel mt-16">
                    <h3>My Connections ({connections.length})</h3>
                    {connections.length === 0 ? (
                      <p className="muted small-p">Connect with healthcare professionals to grow your network.</p>
                    ) : (
                      <div className="connections-quick-list">
                        {connections.map((c: any) => (
                          <div className="conn-row" key={c.connectionId}>
                            <span className="avatar small">{c.partner.avatarInitials}</span>
                            <div className="grow">
                              <strong>{c.partner.fullName}</strong>
                              <small>{c.partner.specialization}</small>
                            </div>
                            {c.status === 'PENDING' && !c.isRequester && (
                              <button className="primary tiny" onClick={() => handleUpdateConnection(c.connectionId, 'ACCEPT')}>
                                Accept
                              </button>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ============================================================== */}
          {/* VIEW: EXPLORE (SKILLS, CAREERS, PUBMED RESEARCH) */}
          {/* ============================================================== */}
          {page === 'Explore' && (
            <div className="explore-layout">
              <PageTitle
                title="Healthcare Intelligence & Literature Discovery"
                subtitle="Explore standardized healthcare competencies, career profiles, and live PubMed biomedical literature."
              />

              {/* PubMed Live Literature Search */}
              <section className="panel mb-24">
                <div className="section-heading">
                  <div>
                    <h3>PubMed Biomedical Literature Discovery</h3>
                    <p>Live search powered by NCBI E-utilities returning verified research publications</p>
                  </div>
                  <span className="soft-tag">Live NCBI E-utilities</span>
                </div>

                <div className="search-panel">
                  <Search size={20} />
                  <input
                    placeholder="Search clinical topics (e.g., patient safety in nursing, CAR-T clinical trials, epidemiology)..."
                    value={researchQuery}
                    onChange={e => setResearchQuery(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && handleSearchResearch()}
                  />
                  <button className="primary" disabled={busy || researchQuery.trim().length < 2} onClick={handleSearchResearch}>
                    {busy ? <LoaderCircle className="spin" size={16} /> : <Search size={16} />} Search PubMed
                  </button>
                </div>
                <small className="disclaimer mt-8">Literature discovery only, not medical advice. Always consult authoritative source publications.</small>

                {researchResults?.articles && (
                  <div className="research-results mt-16">
                    <h4>{researchResults.count} peer-reviewed articles found for "{researchResults.query}"</h4>
                    <div className="research-list">
                      {researchResults.articles.map((art: any) => (
                        <div className="research-card" key={art.pmid}>
                          <div className="research-icon"><Microscope size={20} /></div>
                          <div className="grow">
                            <h4>{art.title}</h4>
                            <p className="muted">{art.source} {art.publicationDate ? `· ${art.publicationDate}` : ''}</p>
                            <small>PubMed PMID: {art.pmid}</small>
                          </div>
                          <a href={art.url} target="_blank" rel="noopener noreferrer" className="outline small">
                            View Paper <ArrowUpRight size={14} />
                          </a>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </section>

              {/* Skills Taxonomy Catalog */}
              <section className="panel mb-24">
                <div className="section-heading">
                  <div>
                    <h3>Centralized Healthcare Skill Taxonomy</h3>
                    <p>20 standardized competencies across clinical, laboratory, informatics, and administrative tracks</p>
                  </div>
                </div>

                <div className="skills-taxonomy-grid">
                  {skillsCatalog.map((sk: any) => (
                    <div className="skill-card-modern" key={sk.id}>
                      <span className="soft-tag">{sk.category}</span>
                      <h4>{sk.name}</h4>
                      <p>{sk.description}</p>
                      <small><b>Related Careers:</b> {sk.relatedCareers}</small>
                      {role === 'STUDENT' && (
                        <button
                          className="outline tiny mt-8"
                          onClick={() => handleAddSkill(sk.name, 'Intermediate')}
                        >
                          + Add to My Skills
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              </section>
            </div>
          )}

          {/* ============================================================== */}
          {/* VIEW: CAREER & OPPORTUNITIES */}
          {/* ============================================================== */}
          {(page === 'Career & Opportunities' || page === 'Opportunities') && (
            <div className="career-opportunities-layout">
              <PageTitle
                title="Career Discovery, Skill Gap & Opportunities"
                subtitle="Calibrated career mapping, real-time market readiness intelligence, and verified internships & jobs."
              />

              {/* Navigation Tabs */}
              <div className="subtabs">
                <button className={careerTab === 'paths' ? 'active' : ''} onClick={() => setCareerTab('paths')}>
                  <Compass size={16} /> Career Pathways
                </button>
                <button className={careerTab === 'readiness' ? 'active' : ''} onClick={() => setCareerTab('readiness')}>
                  <TrendingUp size={16} /> Market Readiness & Skill Gap
                </button>
                <button className={careerTab === 'jobs' ? 'active' : ''} onClick={() => setCareerTab('jobs')}>
                  <BriefcaseMedical size={16} /> Jobs & Internships ({opportunities.length})
                </button>
                <button className={careerTab === 'applications' ? 'active' : ''} onClick={() => setCareerTab('applications')}>
                  <CheckCircle2 size={16} /> My Applications ({myApplications.length})
                </button>
              </div>

              {/* TAB 1: CAREER PATHWAYS */}
              {careerTab === 'paths' && (
                <div className="tab-pane">
                  <div className="career-cards-grid">
                    {careerRecommendations.map((c: any) => (
                      <div className="career-detail-card" key={c.careerId}>
                        <div className="career-card-header">
                          <div>
                            <span className="soft-tag">{c.category}</span>
                            <h3>{c.careerName}</h3>
                          </div>
                          <span className="match-pill large">{c.matchPercentage}% Match</span>
                        </div>
                        <p className="career-desc">{c.description}</p>

                        <div className="career-metrics-row">
                          <div><small>Salary Band</small><strong>{c.averageSalaryRange}</strong></div>
                          <div><small>Market Outlook</small><strong className="text-teal">{c.marketOutlook}</strong></div>
                        </div>

                        <div className="why-matched">
                          <strong><Sparkles size={14} /> Why this matches you:</strong>
                          <p>{c.explanation}</p>
                        </div>

                        <div className="skills-comparison">
                          <div>
                            <span className="text-teal font-bold text-xs">Skills You Possess ({c.matchedSkills?.length || 0}):</span>
                            <div className="chips-row">
                              {(c.matchedSkills || []).map((s: string) => (
                                <span className="chip-green" key={s}>✓ {s}</span>
                              ))}
                              {c.matchedSkills?.length === 0 && <small className="muted">None listed yet</small>}
                            </div>
                          </div>
                          <div className="mt-8">
                            <span className="text-orange font-bold text-xs">Skills to Develop ({c.missingSkills?.length || 0}):</span>
                            <div className="chips-row">
                              {(c.missingSkills || []).map((s: string) => (
                                <span className="chip-orange" key={s}>+ {s}</span>
                              ))}
                            </div>
                          </div>
                        </div>

                        <div className="career-card-footer">
                          <small>Recommended Certifications: <b>{c.recommendedCertifications?.join(', ') || 'Domain standard'}</b></small>
                          <button
                            className="primary small mt-8"
                            onClick={() => {
                              setAiTargetCareer(c.careerName);
                              setAiStudioTab('skill_plan');
                              setPage('AI Learning Studio');
                              handleRunAiAction('skill_plan');
                            }}
                          >
                            Generate Skill Improvement Plan <ChevronRight size={14} />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* TAB 2: MARKET READINESS & SKILL GAP */}
              {careerTab === 'readiness' && (
                <div className="tab-pane">
                  <div className="readiness-overview-grid">
                    <div className="panel readiness-score-box">
                      <span className="eyebrow">LIVE MARKET ALIGNMENT</span>
                      <h2>{marketReadiness?.readinessScore || 65}% Readiness</h2>
                      <div className={`readiness-badge ${(marketReadiness?.readinessTier || 'Developing').toLowerCase().replace(/\\s+/g, '-')}`}>
                        {marketReadiness?.readinessTier || 'Developing Alignment'}
                      </div>
                      <p className="muted mt-8">
                        Analyzed against {marketReadiness?.opportunitiesAnalyzed || 5} active healthcare opportunities in the system.
                      </p>
                      <small className="muted">{marketReadiness?.freshness}</small>
                    </div>

                    <div className="panel in-demand-box">
                      <div className="section-heading">
                        <div>
                          <h3>Most In-Demand Healthcare Skills</h3>
                          <p>Aggregated from verified platform opportunities</p>
                        </div>
                      </div>
                      <div className="demand-bars">
                        {(marketReadiness?.inDemandSkills || []).map((s: any) => (
                          <div className="demand-bar-row" key={s.skill}>
                            <div className="bar-label">
                              <span>{s.skill}</span>
                              <span className={s.possessed ? 'text-teal' : 'text-orange'}>
                                {s.possessed ? '✓ Possessed' : 'Needs Development'}
                              </span>
                            </div>
                            <div className="bar-track">
                              <div
                                className={`bar-fill ${s.possessed ? 'green' : 'orange'}`}
                                style={{ width: `${Math.min(100, s.demandCount * 25)}%` }}
                              />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>

                  <div className="panel mt-16">
                    <div className="section-heading">
                      <div>
                        <h3>Recommended Next Skills to Close Gaps</h3>
                        <p>Focusing on these competencies will increase your opportunity match by up to 35%</p>
                      </div>
                    </div>
                    <div className="gap-next-row">
                      {(marketReadiness?.recommendedNextSkills || []).map((skillName: string) => (
                        <div className="gap-action-card" key={skillName}>
                          <span className="soft-tag">High Priority Gap</span>
                          <h4>{skillName}</h4>
                          <p>Frequently required by premier clinical networks and research sponsors.</p>
                          <div className="btn-row">
                            <button className="primary small" onClick={() => { setAiTopic(skillName); setPage('AI Learning Studio'); setAiStudioTab('builder'); handleRunAiAction('notes'); }}>
                              Study with AI <ChevronRight size={14} />
                            </button>
                            <button className="outline small" onClick={() => handleAddSkill(skillName, 'Intermediate')}>
                              Mark as Learned
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {/* TAB 3: JOBS & INTERNSHIPS */}
              {careerTab === 'jobs' && (
                <div className="tab-pane">
                  {/* Filter chips */}
                  <div className="opp-filters">
                    {['ALL', 'INTERNSHIP', 'FULL_TIME', 'RESEARCH', 'CLINICAL_PLACEMENT'].map(t => (
                      <button
                        key={t}
                        className={`filter-btn ${oppTypeFilter === t ? 'active' : ''}`}
                        onClick={() => setOppTypeFilter(t)}
                      >
                        {t.replace(/_/g, ' ')}
                      </button>
                    ))}
                    {role === 'RECRUITER' && (
                      <button className="primary small ml-auto" onClick={() => setShowNewOppModal(true)}>
                        <Plus size={15} /> Post Opportunity
                      </button>
                    )}
                  </div>

                  <div className="card-grid mt-16">
                    {opportunities.map((opp: any) => (
                      <div className="opportunity-card-rich" key={opp.id}>
                        <div className="opp-top">
                          <span className="opp-icon"><BriefcaseMedical size={20} /></span>
                          <span className="match-pill">{opp.matchPercentage}% Match</span>
                        </div>
                        <span className="soft-tag">{opp.type}</span>
                        <h3>{opp.title}</h3>
                        <strong>{opp.organization}</strong>
                        <p className="opp-summary">{opp.eligibilitySummary}</p>

                        <div className="opp-details-meta">
                          <span>📍 {opp.location} ({opp.workMode})</span>
                          <span>⏳ {opp.duration || 'Flexible'}</span>
                          {opp.stipendOrSalary && <span>💰 {opp.stipendOrSalary}</span>}
                          {opp.deadline && <span>📅 Deadline: {opp.deadline}</span>}
                        </div>

                        <div className="opp-skills-box">
                          <small>Required Skills:</small>
                          <div className="skill-mini-chips">
                            {(opp.requiredSkills || []).map((s: string) => (
                              <span
                                key={s}
                                className={`mini-chip ${(opp.matchedSkills || []).includes(s) ? 'matched' : 'missing'}`}
                              >
                                {(opp.matchedSkills || []).includes(s) ? '✓ ' : '+ '}{s}
                              </span>
                            ))}
                          </div>
                        </div>

                        <div className="opp-actions">
                          <button
                            className={`save-btn ${opp.isSaved ? 'saved' : ''}`}
                            onClick={() => handleToggleSaveOpportunity(opp.id)}
                            title="Save opportunity"
                          >
                            <Bookmark size={18} />
                          </button>
                          {opp.isApplied ? (
                            <span className="applied-tag"><Check size={14} /> Applied</span>
                          ) : (
                            <button
                              className="primary flex-grow"
                              onClick={() => { setSelectedOppForApply(opp); setApplyCoverNote(`I am applying for ${opp.title} at ${opp.organization}.`); }}
                            >
                              Apply Now <ChevronRight size={16} />
                            </button>
                          )}
                          {opp.officialUrl && (
                            <a href={opp.officialUrl} target="_blank" rel="noopener noreferrer" className="outline icon-only" title="Official Source">
                              <ArrowUpRight size={16} />
                            </a>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* TAB 4: MY APPLICATIONS */}
              {careerTab === 'applications' && (
                <div className="tab-pane">
                  <div className="panel">
                    <h3>Submitted Healthcare Applications ({myApplications.length})</h3>
                    {myApplications.length === 0 ? (
                      <div className="empty-state">
                        <BriefcaseMedical size={32} />
                        <strong>No submitted applications yet</strong>
                        <p>Browse verified internships and jobs to submit your profile.</p>
                      </div>
                    ) : (
                      <div className="table-wrap mt-16">
                        <table>
                          <thead>
                            <tr>
                              <th>Opportunity</th>
                              <th>Applicant</th>
                              <th>Applied Date</th>
                              <th>Status</th>
                            </tr>
                          </thead>
                          <tbody>
                            {myApplications.map((app: any) => (
                              <tr key={app.id}>
                                <td><b>{app.opportunityId}</b></td>
                                <td>{app.applicantName} ({app.applicantSpecialization})</td>
                                <td>{new Date(app.appliedAt).toLocaleDateString()}</td>
                                <td><span className={`status-pill ${app.status.toLowerCase()}`}>{app.status}</span></td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* ============================================================== */}
          {/* VIEW: AI LEARNING STUDIO & MY CONTENT */}
          {/* ============================================================== */}
          {(page === 'AI Learning Studio' || page === 'My Content') && (
            <div className="ai-studio-layout">
              <PageTitle
                title="AI Learning Studio & Content Ecosystem"
                subtitle="Author personal healthcare notes, generate clinical study aids, export real PowerPoint presentations, and build study schedules."
              />

              {/* Studio Tabs */}
              <div className="subtabs">
                <button className={aiStudioTab === 'notes' ? 'active' : ''} onClick={() => setAiStudioTab('notes')}>
                  <FileText size={16} /> My Notes Library ({userNotes.length})
                </button>
                <button className={aiStudioTab === 'builder' ? 'active' : ''} onClick={() => setAiStudioTab('builder')}>
                  <Brain size={16} /> Note Builder (Manual & AI)
                </button>
                <button className={aiStudioTab === 'roadmap' ? 'active' : ''} onClick={() => setAiStudioTab('roadmap')}>
                  <Compass size={16} /> Learning Roadmap
                </button>
                <button className={aiStudioTab === 'presentation' ? 'active' : ''} onClick={() => setAiStudioTab('presentation')}>
                  <Presentation size={16} /> PowerPoint Generator
                </button>
                <button className={aiStudioTab === 'study_plan' ? 'active' : ''} onClick={() => setAiStudioTab('study_plan')}>
                  <Calendar size={16} /> Study Plan Generator
                </button>
                <button className={aiStudioTab === 'explainer' ? 'active' : ''} onClick={() => setAiStudioTab('explainer')}>
                  <Sparkles size={16} /> Concept Explainer
                </button>
              </div>

              {/* TAB 1: MY NOTES LIBRARY */}
              {aiStudioTab === 'notes' && (
                <div className="tab-pane">
                  <div className="notes-library-header">
                    <div>
                      <h3>Saved Healthcare Notes</h3>
                      <p className="muted">Persisted in MySQL with full search and revision points.</p>
                    </div>
                    <button className="primary" onClick={() => setAiStudioTab('builder')}>
                      <Plus size={16} /> Create New Note
                    </button>
                  </div>

                  {userNotes.length === 0 ? (
                    <div className="panel empty-state">
                      <FileText size={32} />
                      <strong>No notes saved yet</strong>
                      <p>Create manual revision notes or use the AI generator to start your study library.</p>
                    </div>
                  ) : (
                    <div className="notes-grid">
                      {userNotes.map((note: any) => (
                        <div className="note-library-card" key={note.id}>
                          <div className="note-top">
                            <span className="soft-tag">{note.category}</span>
                            <span className={note.isAiGenerated ? 'ai-badge' : 'manual-badge'}>
                              {note.isAiGenerated ? 'AI-Generated' : 'Manual Note'}
                            </span>
                          </div>
                          <h4>{note.title}</h4>
                          <p className="note-preview-text">{note.content}</p>
                          <small className="muted">Topic: <b>{note.topic}</b> · Level: {note.level}</small>
                          <div className="note-card-actions mt-12">
                            <button className="outline small" onClick={() => setSelectedNote(note)}>
                              View Full Note <ChevronRight size={14} />
                            </button>
                            <button className="icon-btn delete-btn" onClick={() => handleDeleteNote(note.id)} title="Delete note">
                              <Trash2 size={16} />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Selected Note Modal */}
                  {selectedNote && (
                    <div className="modal-scrim" onClick={() => setSelectedNote(null)}>
                      <div className="modal-content large" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                          <div>
                            <span className="soft-tag">{selectedNote.category}</span>
                            <h2>{selectedNote.title}</h2>
                          </div>
                          <button className="icon-btn" onClick={() => setSelectedNote(null)}><X size={20} /></button>
                        </div>
                        <div className="modal-body">
                          <p className="note-lead">{selectedNote.content}</p>
                          {/* If sections exist */}
                          {selectedNote.sectionsJson && (
                            <div className="note-sections-render">
                              {JSON.parse(selectedNote.sectionsJson || '[]').map((s: any, idx: number) => (
                                <div key={idx} className="note-sec-block">
                                  <h4>{s.heading}</h4>
                                  <p>{s.content}</p>
                                  {s.items && (
                                    <ul>
                                      {s.items.map((it: string, i: number) => <li key={i}>{it}</li>)}
                                    </ul>
                                  )}
                                </div>
                              ))}
                            </div>
                          )}

                          {/* Self test */}
                          {selectedNote.selfTestJson && (
                            <div className="self-test-render mt-16">
                              <h4>Active Recall Self-Test</h4>
                              {JSON.parse(selectedNote.selfTestJson || '[]').map((q: any, i: number) => (
                                <div key={i} className="quiz-q">
                                  <b>Q{i + 1}: {q.question}</b>
                                  <p>{q.answer}</p>
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* TAB 2: NOTE BUILDER (MANUAL & AI) */}
              {aiStudioTab === 'builder' && (
                <div className="tab-pane">
                  <div className="builder-split">
                    {/* Manual Authoring Form */}
                    <div className="panel">
                      <div className="section-heading">
                        <div>
                          <h3>Manual Note Creator</h3>
                          <p>Author your own revision notes, study summaries, and clinical observations</p>
                        </div>
                        <span className="soft-tag">Manual Entry</span>
                      </div>

                      <form onSubmit={handleSaveManualNote} className="form-stack">
                        <label>
                          Note Title
                          <input
                            required
                            placeholder="e.g. Surgical Time-Out & Infection Control Protocol"
                            value={manualNoteForm.title}
                            onChange={e => setManualNoteForm({ ...manualNoteForm, title: e.target.value })}
                          />
                        </label>
                        <div className="two-col">
                          <label>
                            Topic
                            <input
                              placeholder="e.g. Patient safety"
                              value={manualNoteForm.topic}
                              onChange={e => setManualNoteForm({ ...manualNoteForm, topic: e.target.value })}
                            />
                          </label>
                          <label>
                            Category
                            <select
                              value={manualNoteForm.category}
                              onChange={e => setManualNoteForm({ ...manualNoteForm, category: e.target.value })}
                            >
                              <option>Clinical Knowledge</option>
                              <option>Laboratory & Diagnostics</option>
                              <option>Health Informatics</option>
                              <option>Research & Epidemiology</option>
                              <option>Healthcare Administration</option>
                            </select>
                          </label>
                        </div>
                        <label>
                          Note Body & Key Clinical Points
                          <textarea
                            rows={8}
                            required
                            placeholder="Type structured clinical notes, protocols, key definitions, and considerations..."
                            value={manualNoteForm.content}
                            onChange={e => setManualNoteForm({ ...manualNoteForm, content: e.target.value })}
                          />
                        </label>
                        <label>
                          Tags (comma separated)
                          <input
                            value={manualNoteForm.tags}
                            onChange={e => setManualNoteForm({ ...manualNoteForm, tags: e.target.value })}
                          />
                        </label>
                        <button className="primary" disabled={busy}>
                          {busy ? <LoaderCircle className="spin" size={16} /> : <CheckCircle2 size={16} />} Save to My Notes
                        </button>
                      </form>
                    </div>

                    {/* AI Note Generator Form */}
                    <div className="panel">
                      <div className="section-heading">
                        <div>
                          <h3>AI Structured Study Aid Generator</h3>
                          <p>Generate topic outlines with active recall questions</p>
                        </div>
                        <span className="ai-badge">AI Assistant</span>
                      </div>

                      <div className="form-stack">
                        <label>
                          Topic
                          <input
                            value={aiTopic}
                            onChange={e => setAiTopic(e.target.value)}
                            placeholder="e.g. Clinical trials, Pharmacovigilance, Epidemiology"
                          />
                        </label>
                        <label>
                          Learner Level
                          <select value={aiLevel} onChange={e => setAiLevel(e.target.value)}>
                            <option>Beginner</option>
                            <option>Intermediate</option>
                            <option>Advanced</option>
                          </select>
                        </label>
                        <button className="primary" disabled={busy} onClick={() => handleRunAiAction('notes')}>
                          {busy ? <LoaderCircle className="spin" size={16} /> : <Brain size={16} />} Generate Study Aid
                        </button>

                        {aiResult?.sections && (
                          <div className="ai-output-box mt-16">
                            <div className="output-header">
                              <h4>{aiResult.topic} (Generated Aid)</h4>
                              <button className="outline small" onClick={handleSaveGeneratedNote}>
                                Save to My Notes Library
                              </button>
                            </div>
                            <p className="disclaimer">{aiResult.disclaimer}</p>
                            {aiResult.sections.map((s: any, idx: number) => (
                              <div key={idx} className="note-sec-block">
                                <h5>{s.heading}</h5>
                                {s.content && <p>{s.content}</p>}
                                {s.items && (
                                  <ul>
                                    {s.items.map((it: string, i: number) => <li key={i}>{it}</li>)}
                                  </ul>
                                )}
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* TAB 3: LEARNING ROADMAP */}
              {aiStudioTab === 'roadmap' && (
                <div className="tab-pane">
                  <div className="tool-layout">
                    <section className="panel tool-form">
                      <div className="section-heading">
                        <div>
                          <h3>Personalized Learning Roadmap</h3>
                          <p>Generate week-by-week structured milestones</p>
                        </div>
                      </div>
                      <label>
                        Goal
                        <textarea
                          rows={3}
                          value={aiGoal}
                          onChange={e => setAiGoal(e.target.value)}
                        />
                      </label>
                      <label>
                        Duration
                        <select value={aiDurationWeeks} onChange={e => setAiDurationWeeks(Number(e.target.value))}>
                          {[2, 3, 4, 6, 8, 12, 16, 24].map(n => <option key={n} value={n}>{n} weeks</option>)}
                        </select>
                      </label>
                      <button className="primary" disabled={busy} onClick={() => handleRunAiAction('roadmap')}>
                        {busy ? <LoaderCircle className="spin" size={16} /> : <Compass size={16} />} Generate Roadmap
                      </button>
                    </section>

                    <section className="panel output-panel">
                      <h3>Generated Pathway</h3>
                      {aiResult?.weeks ? (
                        <div className="roadmap-render">
                          <p className="disclaimer">{aiResult.disclaimer}</p>
                          {aiResult.weeks.map((w: any) => (
                            <div className="week-card" key={w.week}>
                              <span className="week-number">W{w.week}</span>
                              <div className="grow">
                                <h4>{w.title}</h4>
                                <p>{w.objective}</p>
                                <ul>
                                  {w.activities?.map((act: string, i: number) => <li key={i}>{act}</li>)}
                                </ul>
                                <small><b>Milestone:</b> {w.milestone}</small>
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="empty-state">
                          <Compass size={32} />
                          <strong>Roadmap will appear here</strong>
                          <p>Define your learning goal and duration to generate a customized week-by-week path.</p>
                        </div>
                      )}
                    </section>
                  </div>
                </div>
              )}

              {/* TAB 4: PRESENTATION GENERATOR (.PPTX) */}
              {aiStudioTab === 'presentation' && (
                <div className="tab-pane">
                  <div className="presentation-card">
                    <div className="presentation-illustration">
                      <Presentation size={64} />
                      <div className="slide-mini"><span /><span /><span /></div>
                    </div>
                    <div className="presentation-form">
                      <span className="eyebrow">POWERPOINT STUDY DECK BUILDER</span>
                      <h2>Real .pptx Slide Generation</h2>
                      <p className="muted">Generates a genuine PowerPoint file (.pptx) with structured learning objectives, core concepts, case study prompts, and responsible-use citations.</p>

                      <label>
                        Presentation Topic
                        <input value={aiTopic} onChange={e => setAiTopic(e.target.value)} />
                      </label>
                      <label>
                        Slide Count
                        <select value={aiSlideCount} onChange={e => setAiSlideCount(Number(e.target.value))}>
                          <option value={4}>4 slides (Summary)</option>
                          <option value={6}>6 slides (Standard Lecture)</option>
                          <option value={8}>8 slides (Comprehensive)</option>
                          <option value={10}>10 slides (In-depth)</option>
                        </select>
                      </label>
                      <button className="primary" disabled={busy} onClick={() => handleRunAiAction('presentation')}>
                        {busy ? <LoaderCircle className="spin" size={16} /> : <Presentation size={16} />} Generate & Download .pptx Deck
                      </button>
                      <small className="muted">Generated binary file formatted with python-pptx.</small>
                    </div>
                  </div>
                </div>
              )}

              {/* TAB 5: STUDY PLANS */}
              {aiStudioTab === 'study_plan' && (
                <div className="tab-pane">
                  <div className="tool-layout">
                    <section className="panel tool-form">
                      <div className="section-heading">
                        <div>
                          <h3>AI Study Schedule Generator</h3>
                          <p>Create a weekly study plan calibrated to your target career and available time</p>
                        </div>
                      </div>
                      <label>
                        Target Healthcare Career
                        <input value={aiTargetCareer} onChange={e => setAiTargetCareer(e.target.value)} />
                      </label>
                      <label>
                        Available Study Hours Per Week
                        <select value={aiHoursPerWeek} onChange={e => setAiHoursPerWeek(Number(e.target.value))}>
                          <option value={5}>5 hours/week (Light)</option>
                          <option value={10}>10 hours/week (Moderate)</option>
                          <option value={15}>15 hours/week (Intensive)</option>
                          <option value={20}>20 hours/week (Fast Track)</option>
                        </select>
                      </label>
                      <button className="primary" disabled={busy} onClick={() => handleRunAiAction('study_plan')}>
                        {busy ? <LoaderCircle className="spin" size={16} /> : <Calendar size={16} />} Generate Weekly Schedule
                      </button>
                    </section>

                    <section className="panel output-panel">
                      <h3>Your Custom Study Schedule</h3>
                      {aiResult?.schedule ? (
                        <div className="schedule-render">
                          <p className="notice-lead"><b>{aiResult.recommendation}</b> ({aiResult.dailyHours} hrs/day)</p>
                          <div className="days-stack">
                            {aiResult.schedule.map((day: any) => (
                              <div className="day-card" key={day.day}>
                                <div className="day-header">
                                  <strong>{day.day}</strong>
                                  <span className="soft-tag">{day.allocatedHours} hours</span>
                                </div>
                                <p className="font-bold text-teal">{day.focusTopic}</p>
                                <ul>
                                  {day.activities?.map((act: string, i: number) => <li key={i}>{act}</li>)}
                                </ul>
                                <small><b>Daily Milestone:</b> {day.milestone}</small>
                              </div>
                            ))}
                          </div>
                        </div>
                      ) : (
                        <div className="empty-state">
                          <Calendar size={32} />
                          <strong>Study plan will appear here</strong>
                          <p>Select your weekly hours to get daily structured time allocations.</p>
                        </div>
                      )}
                    </section>
                  </div>
                </div>
              )}

              {/* TAB 6: CONCEPT EXPLAINER */}
              {aiStudioTab === 'explainer' && (
                <div className="tab-pane">
                  <div className="tool-layout">
                    <section className="panel tool-form">
                      <div className="section-heading">
                        <div>
                          <h3>Healthcare Concept Explainer</h3>
                          <p>Explain complex biomedical or clinical topics at three distinct depth tiers</p>
                        </div>
                      </div>
                      <label>
                        Healthcare Concept
                        <input value={aiConcept} onChange={e => setAiConcept(e.target.value)} />
                      </label>
                      <label>
                        Explanation Depth
                        <select value={aiExplainDepth} onChange={e => setAiExplainDepth(e.target.value)}>
                          <option value="Beginner">Beginner (Foundational, Analogies)</option>
                          <option value="Intermediate">Intermediate (Clinical Practice & Protocols)</option>
                          <option value="Advanced">Advanced (Mechanistic, Trial Evidence, Audits)</option>
                        </select>
                      </label>
                      <button className="primary" disabled={busy} onClick={() => handleRunAiAction('explain')}>
                        {busy ? <LoaderCircle className="spin" size={16} /> : <Sparkles size={16} />} Explain Concept
                      </button>
                    </section>

                    <section className="panel output-panel">
                      <h3>Concept Breakdown</h3>
                      {aiResult?.explanation ? (
                        <div className="explainer-render">
                          <span className="soft-tag">{aiResult.depth} Level</span>
                          <h4>{aiResult.concept}</h4>
                          <p className="explainer-text">{aiResult.explanation}</p>
                          <div className="key-points-box">
                            <b>Key Takeaways:</b>
                            <ul>
                              {aiResult.keyPoints?.map((kp: string, i: number) => <li key={i}>{kp}</li>)}
                            </ul>
                          </div>
                          <p className="clinical-context"><b>Clinical Application:</b> {aiResult.clinicalContext}</p>
                        </div>
                      ) : (
                        <div className="empty-state">
                          <Sparkles size={32} />
                          <strong>Explanation will appear here</strong>
                          <p>Enter any medical, nursing, or research term to receive calibrated explanations.</p>
                        </div>
                      )}
                    </section>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* ============================================================== */}
          {/* VIEW: CLASSES (FOR MENTOR / EDUCATOR) */}
          {/* ============================================================== */}
          {page === 'Classes' && (
            <div className="classes-layout">
              <PageTitle
                title="Learning Classes & Teaching Workspace"
                subtitle="Manage curriculum syllabi, share clinical reading materials, and review student progress."
              />

              <div className="card-grid">
                <div className="class-card panel">
                  <div className="class-header">
                    <span className="soft-tag">Semester 5</span>
                    <h3>Clinical Epidemiology & Study Design</h3>
                    <small>32 Enrolled Students</small>
                  </div>
                  <p>Covers observational studies, cohort bias, randomized trial appraisal, and IRB protocol compliance.</p>
                  <div className="class-stats">
                    <div><b>4 Modules</b><span>Completed</span></div>
                    <div><b>88% Avg</b><span>Quiz Score</span></div>
                  </div>
                  <button className="outline full mt-12" onClick={() => { setAiTopic('Epidemiological Study Design'); setPage('AI Learning Studio'); setAiStudioTab('builder'); handleRunAiAction('notes'); }}>
                    Generate Teaching Material <ChevronRight size={14} />
                  </button>
                </div>

                <div className="class-card panel">
                  <div className="class-header">
                    <span className="soft-tag">Semester 3</span>
                    <h3>Evidence-Based Patient Safety</h3>
                    <small>45 Enrolled Students</small>
                  </div>
                  <p>Infection mitigation, hospital incident audits, surgical checklist protocols, and medication safety.</p>
                  <div className="class-stats">
                    <div><b>3 Modules</b><span>Completed</span></div>
                    <div><b>91% Avg</b><span>Quiz Score</span></div>
                  </div>
                  <button className="outline full mt-12" onClick={() => { setAiTopic('Patient safety culture'); setPage('AI Learning Studio'); setAiStudioTab('presentation'); handleRunAiAction('presentation'); }}>
                    Generate Slide Deck <ChevronRight size={14} />
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* ============================================================== */}
          {/* VIEW: PROFILE */}
          {/* ============================================================== */}
          {page === 'Profile' && (
            <div className="profile-layout">
              <PageTitle
                title="Professional Profile & Competencies"
                subtitle="Your verified healthcare identity, skills taxonomy, and career preferences."
              />

              <div className="profile-panel panel">
                <div className="profile-cover" />
                <div className="profile-details">
                  <div className="profile-avatar-row">
                    <span className="avatar large">{user.avatarInitials}</span>
                    <div className="profile-titles">
                      <h2>{user.fullName}</h2>
                      <span className="soft-tag">{roleBadgeLabel}</span>
                      <p className="muted">{user.headline || `${user.specialization || 'Healthcare'} · ${user.institution || user.location}`}</p>
                    </div>
                    {role === 'STUDENT' && (
                      <button className="primary ml-auto" onClick={() => setShowOnboarding(true)}>
                        <SlidersHorizontal size={15} /> Update Career Questionnaire
                      </button>
                    )}
                  </div>

                  <div className="profile-fields-grid mt-16">
                    <div><small>Email</small><strong>{user.email}</strong></div>
                    <div><small>Specialization</small><strong>{user.specialization || 'Healthcare'}</strong></div>
                    <div><small>Academic Level / Experience</small><strong>{user.experienceLevel || 'N/A'}</strong></div>
                    <div><small>Institution / Organization</small><strong>{user.institution || 'AIIMS'}</strong></div>
                    <div><small>Location</small><strong>{user.location || 'India'}</strong></div>
                    <div><small>Verification Status</small><strong className="text-teal">{user.verificationStatus || 'VERIFIED'}</strong></div>
                  </div>

                  {user.bio && (
                    <div className="profile-bio-box mt-16">
                      <small>About</small>
                      <p>{user.bio}</p>
                    </div>
                  )}

                  {/* Skills Section */}
                  <div className="profile-skills-section mt-24">
                    <div className="section-heading">
                      <div>
                        <h3>My Healthcare Skills & Proficiency</h3>
                        <p>Persisted in MySQL with verification evidence tracking</p>
                      </div>
                    </div>

                    <div className="user-skills-grid">
                      {userSkills.map((us: any) => (
                        <div className="user-skill-chip" key={us.id}>
                          <div>
                            <strong>{us.skillName}</strong>
                            <span className="prof-tag">{us.proficiency}</span>
                            <small className="muted">{us.evidenceType}</small>
                          </div>
                          <button className="icon-btn delete-mini" onClick={() => handleDeleteSkill(us.id)} title="Remove skill">
                            <X size={14} />
                          </button>
                        </div>
                      ))}
                      {userSkills.length === 0 && (
                        <p className="muted">No skills added yet. Complete the onboarding questionnaire or add skills above.</p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ============================================================== */}
          {/* VIEW: AUDIT LOGS (ADMIN ONLY) */}
          {/* ============================================================== */}
          {page === 'Audit Logs' && (
            <div className="audit-layout">
              <PageTitle
                title="Security & Compliance Audit Trail"
                subtitle="Real-time tamper-resistant log of all authentication, profile, questionnaire, career, and content events."
              />

              <div className="panel">
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Timestamp</th>
                        <th>Event Type</th>
                        <th>Actor</th>
                        <th>Target</th>
                        <th>Status</th>
                        <th>IP Address</th>
                      </tr>
                    </thead>
                    <tbody>
                      {auditLogs.map((log: any) => (
                        <tr key={log.id}>
                          <td>{new Date(log.createdAt).toLocaleString()}</td>
                          <td><span className="role-tag">{log.eventType}</span></td>
                          <td>{log.actorEmail || log.actorUserId || 'Anonymous'}</td>
                          <td>{log.targetEntityType}: {log.targetEntityId}</td>
                          <td><span className={`status-pill ${log.resultStatus.toLowerCase()}`}>{log.resultStatus}</span></td>
                          <td><small>{log.ipAddress || '127.0.0.1'}</small></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <footer className="app-footer">
          <span>© 2026 HealthSkill Connect • Healthcare Career Intelligence & Learning Ecosystem</span>
          <span><ShieldCheck size={14} /> Real MySQL Persistence • Evidence-Aware • Not Clinical Advice</span>
        </footer>
      </main>

      {/* ============================================================== */}
      {/* MODAL: STUDENT ONBOARDING QUESTIONNAIRE (PHASE 4) */}
      {/* ============================================================== */}
      {showOnboarding && (
        <div className="modal-scrim">
          <div className="modal-content onboarding-modal" onClick={e => e.stopPropagation()}>
            <div className="onboarding-header">
              <div>
                <span className="hero-tag"><Sparkles size={14} /> CAREER DISCOVERY WIZARD</span>
                <h2>Healthcare Career Questionnaire</h2>
                <p className="muted">Help our recommendation engine understand your background, skills, and goals.</p>
              </div>
              <button className="icon-btn" onClick={() => setShowOnboarding(false)}><X size={20} /></button>
            </div>

            {/* Step Progress Bar */}
            <div className="step-bar">
              {['Education', 'Interests', 'Career Goals', 'Skills', 'Experience', 'Preferences'].map((name, i) => (
                <div key={name} className={`step-item ${onboardingStep === i + 1 ? 'active' : onboardingStep > i + 1 ? 'completed' : ''}`}>
                  <span className="step-num">{i + 1}</span>
                  <span className="step-name">{name}</span>
                </div>
              ))}
            </div>

            <div className="onboarding-step-body">
              {/* STEP 1: EDUCATION */}
              {onboardingStep === 1 && (
                <div className="step-content">
                  <h3>Section A: Educational Background</h3>
                  <div className="two-col">
                    <label>
                      Current Qualification
                      <select
                        value={onboardingData.currentQualification}
                        onChange={e => setOnboardingData({ ...onboardingData, currentQualification: e.target.value })}
                      >
                        <option>Undergraduate</option>
                        <option>Postgraduate / Master's</option>
                        <option>Diploma / Certification</option>
                        <option>Doctorate / Residency</option>
                      </select>
                    </label>
                    <label>
                      Degree or Course
                      <input
                        placeholder="e.g. BSc Nursing, MBBS, BPharm, BPT, BMLT"
                        value={onboardingData.degreeOrCourse}
                        onChange={e => setOnboardingData({ ...onboardingData, degreeOrCourse: e.target.value })}
                      />
                    </label>
                  </div>
                  <div className="two-col">
                    <label>
                      Specialization / Major
                      <input
                        placeholder="e.g. Nursing, Clinical Research, Pharmacy"
                        value={onboardingData.specialization}
                        onChange={e => setOnboardingData({ ...onboardingData, specialization: e.target.value })}
                      />
                    </label>
                    <label>
                      Institution / University
                      <input
                        placeholder="e.g. AIIMS, Madras Medical College"
                        value={onboardingData.institution}
                        onChange={e => setOnboardingData({ ...onboardingData, institution: e.target.value })}
                      />
                    </label>
                  </div>
                  <label>
                    Current Academic Year or Semester
                    <select
                      value={onboardingData.academicYearOrSemester}
                      onChange={e => setOnboardingData({ ...onboardingData, academicYearOrSemester: e.target.value })}
                    >
                      <option>Year 1</option>
                      <option>Year 2</option>
                      <option>Year 3</option>
                      <option>Year 4</option>
                      <option>Year 5</option>
                      <option>Completed / Intern</option>
                    </select>
                  </label>
                </div>
              )}

              {/* STEP 2: HEALTHCARE INTERESTS */}
              {onboardingStep === 2 && (
                <div className="step-content">
                  <h3>Section B: Healthcare Interests</h3>
                  <p className="muted">Select all fields you are curious to explore:</p>
                  <div className="checkbox-pills-wrap">
                    {HEALTHCARE_INTERESTS_OPTIONS.map(interest => {
                      const selected = onboardingData.healthcareInterests.includes(interest);
                      return (
                        <button
                          type="button"
                          key={interest}
                          className={`pill-check-btn ${selected ? 'selected' : ''}`}
                          onClick={() => {
                            setOnboardingData(prev => ({
                              ...prev,
                              healthcareInterests: selected
                                ? prev.healthcareInterests.filter(i => i !== interest)
                                : [...prev.healthcareInterests, interest]
                            }));
                          }}
                        >
                          {selected ? '✓ ' : '+ '}{interest}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* STEP 3: CAREER PREFERENCES */}
              {onboardingStep === 3 && (
                <div className="step-content">
                  <h3>Section C: Career Direction Preferences</h3>
                  <p className="muted">What environments and career types would you like to explore?</p>
                  <div className="checkbox-pills-wrap">
                    {CAREER_PREFERENCES_OPTIONS.map(pref => {
                      const selected = onboardingData.careerPreferences.includes(pref);
                      return (
                        <button
                          type="button"
                          key={pref}
                          className={`pill-check-btn ${selected ? 'selected' : ''}`}
                          onClick={() => {
                            setOnboardingData(prev => ({
                              ...prev,
                              careerPreferences: selected
                                ? prev.careerPreferences.filter(p => p !== pref)
                                : [...prev.careerPreferences, pref]
                            }));
                          }}
                        >
                          {selected ? '✓ ' : '+ '}{pref}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* STEP 4: EXISTING SKILLS */}
              {onboardingStep === 4 && (
                <div className="step-content">
                  <h3>Section D: Current Competencies & Proficiency</h3>
                  <p className="muted">Identify skills you currently have and self-rate your proficiency:</p>
                  <div className="skills-picker-list">
                    {onboardingData.existingSkills.map((sk, idx) => (
                      <div className="skill-edit-row" key={idx}>
                        <input
                          value={sk.name}
                          onChange={e => {
                            const updated = [...onboardingData.existingSkills];
                            updated[idx].name = e.target.value;
                            setOnboardingData({ ...onboardingData, existingSkills: updated });
                          }}
                          placeholder="Skill name"
                        />
                        <select
                          value={sk.proficiency}
                          onChange={e => {
                            const updated = [...onboardingData.existingSkills];
                            updated[idx].proficiency = e.target.value;
                            setOnboardingData({ ...onboardingData, existingSkills: updated });
                          }}
                        >
                          <option>Beginner</option>
                          <option>Basic</option>
                          <option>Intermediate</option>
                          <option>Advanced</option>
                          <option>Expert</option>
                        </select>
                        <button
                          type="button"
                          className="icon-btn delete-mini"
                          onClick={() => {
                            setOnboardingData({
                              ...onboardingData,
                              existingSkills: onboardingData.existingSkills.filter((_, i) => i !== idx)
                            });
                          }}
                        >
                          <X size={16} />
                        </button>
                      </div>
                    ))}
                    <button
                      type="button"
                      className="outline small"
                      onClick={() => {
                        setOnboardingData({
                          ...onboardingData,
                          existingSkills: [...onboardingData.existingSkills, { name: 'Good Clinical Practice (GCP)', proficiency: 'Intermediate' }]
                        });
                      }}
                    >
                      + Add Another Skill
                    </button>
                  </div>
                </div>
              )}

              {/* STEP 5: EXPERIENCE */}
              {onboardingStep === 5 && (
                <div className="step-content">
                  <h3>Section E: Prior Experience</h3>
                  <label>
                    Clinical Exposure, Internships, Projects, Workshops, or Volunteering
                    <textarea
                      rows={5}
                      placeholder="e.g. 6 months hospital ward rotation, clinical audit project, community health camp participation... (or write 'No prior experience')"
                      value={onboardingData.experienceSummary}
                      onChange={e => setOnboardingData({ ...onboardingData, experienceSummary: e.target.value })}
                    />
                  </label>
                </div>
              )}

              {/* STEP 6: LEARNING PREFERENCES & GOALS */}
              {onboardingStep === 6 && (
                <div className="step-content">
                  <h3>Section F: Learning Goals & Preferences</h3>
                  <label>
                    Short-Term Goal (Next 3 to 6 months)
                    <input
                      placeholder="e.g. Master Clinical Research Associate fundamentals and GCP standards"
                      value={onboardingData.shortTermGoals}
                      onChange={e => setOnboardingData({ ...onboardingData, shortTermGoals: e.target.value })}
                    />
                  </label>
                  <label className="mt-12">
                    Long-Term Career Direction (2 to 5 years)
                    <input
                      placeholder="e.g. Lead clinical data management for global clinical trials"
                      value={onboardingData.longTermGoals}
                      onChange={e => setOnboardingData({ ...onboardingData, longTermGoals: e.target.value })}
                    />
                  </label>
                </div>
              )}
            </div>

            <div className="onboarding-footer">
              {onboardingStep > 1 && (
                <button className="outline" onClick={() => setOnboardingStep(onboardingStep - 1)}>
                  <ChevronLeft size={16} /> Previous
                </button>
              )}
              {onboardingStep < 6 ? (
                <button className="primary ml-auto" onClick={() => setOnboardingStep(onboardingStep + 1)}>
                  Next Section <ChevronRight size={16} />
                </button>
              ) : (
                <button className="primary ml-auto" disabled={busy} onClick={submitOnboardingWizard}>
                  {busy ? <LoaderCircle className="spin" size={16} /> : <CheckCircle2 size={16} />}
                  Complete Career Calibration
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* MODAL: CREATE HEALTHCARE POST */}
      {showNewPostModal && (
        <div className="modal-scrim" onClick={() => setShowNewPostModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Create Professional Healthcare Post</h2>
              <button className="icon-btn" onClick={() => setShowNewPostModal(false)}><X size={20} /></button>
            </div>
            <form onSubmit={handleCreatePost} className="form-stack">
              <label>
                Post Title
                <input
                  required
                  placeholder="e.g. Critical Checklist for Clinical Trial Protocol Deviations"
                  value={postForm.title}
                  onChange={e => setPostForm({ ...postForm, title: e.target.value })}
                />
              </label>
              <div className="two-col">
                <label>
                  Category
                  <select
                    value={postForm.category}
                    onChange={e => setPostForm({ ...postForm, category: e.target.value })}
                  >
                    <option value="CLINICAL_INSIGHT">Clinical Insight</option>
                    <option value="CASE_STUDY">Case Study</option>
                    <option value="RESEARCH_UPDATE">Research Update</option>
                    <option value="CAREER_ADVICE">Career Advice</option>
                    <option value="LEARNING_RESOURCE">Learning Resource</option>
                  </select>
                </label>
                <label>
                  Tags (comma separated)
                  <input
                    value={postForm.tags}
                    onChange={e => setPostForm({ ...postForm, tags: e.target.value })}
                    placeholder="Nursing, Clinical, Safety"
                  />
                </label>
              </div>
              <label>
                Content
                <textarea
                  rows={6}
                  required
                  placeholder="Share evidence-based clinical reasoning, educational experiences, or study insights..."
                  value={postForm.content}
                  onChange={e => setPostForm({ ...postForm, content: e.target.value })}
                />
              </label>
              <button className="primary" disabled={busy}>
                {busy ? <LoaderCircle className="spin" size={16} /> : <Send size={16} />} Publish to Healthcare Network
              </button>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: MENTORSHIP REQUEST */}
      {selectedMentor && (
        <div className="modal-scrim" onClick={() => setSelectedMentor(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <span className="soft-tag">Request Mentorship</span>
                <h2>Connect with {selectedMentor.fullName}</h2>
                <small className="muted">{selectedMentor.specialization} · {selectedMentor.institution}</small>
              </div>
              <button className="icon-btn" onClick={() => setSelectedMentor(null)}><X size={20} /></button>
            </div>
            <form onSubmit={handleSubmitMentorshipRequest} className="form-stack">
              <label>
                Inquiry Subject
                <input
                  required
                  value={mentorshipForm.subject}
                  onChange={e => setMentorshipForm({ ...mentorshipForm, subject: e.target.value })}
                />
              </label>
              <label>
                Your Learning Goals
                <input
                  required
                  value={mentorshipForm.goals}
                  onChange={e => setMentorshipForm({ ...mentorshipForm, goals: e.target.value })}
                />
              </label>
              <label>
                Message
                <textarea
                  rows={4}
                  required
                  value={mentorshipForm.message}
                  onChange={e => setMentorshipForm({ ...mentorshipForm, message: e.target.value })}
                />
              </label>
              <button className="primary" disabled={busy}>
                {busy ? <LoaderCircle className="spin" size={16} /> : <Send size={16} />} Send Mentorship Inquiry
              </button>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: APPLY TO OPPORTUNITY */}
      {selectedOppForApply && (
        <div className="modal-scrim" onClick={() => setSelectedOppForApply(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <span className="soft-tag">{selectedOppForApply.type}</span>
                <h2>Apply for {selectedOppForApply.title}</h2>
                <small className="muted">{selectedOppForApply.organization} · {selectedOppForApply.location}</small>
              </div>
              <button className="icon-btn" onClick={() => setSelectedOppForApply(null)}><X size={20} /></button>
            </div>
            <form onSubmit={handleApplyOpportunity} className="form-stack">
              <div className="two-col">
                <label>
                  Applicant Name
                  <input readOnly value={user.fullName} />
                </label>
                <label>
                  Applicant Email
                  <input readOnly value={user.email} />
                </label>
              </div>
              <label>
                Cover Note & Motivation
                <textarea
                  rows={5}
                  required
                  value={applyCoverNote}
                  onChange={e => setApplyCoverNote(e.target.value)}
                />
              </label>
              <button className="primary" disabled={busy}>
                {busy ? <LoaderCircle className="spin" size={16} /> : <CheckCircle2 size={16} />} Submit Application
              </button>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: RECRUITER POST OPPORTUNITY */}
      {showNewOppModal && (
        <div className="modal-scrim" onClick={() => setShowNewOppModal(false)}>
          <div className="modal-content large" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Post Healthcare Opportunity</h2>
              <button className="icon-btn" onClick={() => setShowNewOppModal(false)}><X size={20} /></button>
            </div>
            <form onSubmit={handlePostOpportunity} className="form-stack">
              <div className="two-col">
                <label>
                  Opportunity Title
                  <input
                    required
                    placeholder="e.g. Clinical Research Fellow"
                    value={newOppForm.title}
                    onChange={e => setNewOppForm({ ...newOppForm, title: e.target.value })}
                  />
                </label>
                <label>
                  Organization Name
                  <input
                    required
                    placeholder="e.g. Apollo Hospitals"
                    value={newOppForm.organization}
                    onChange={e => setNewOppForm({ ...newOppForm, organization: e.target.value })}
                  />
                </label>
              </div>

              <div className="two-col">
                <label>
                  Opportunity Type
                  <select
                    value={newOppForm.type}
                    onChange={e => setNewOppForm({ ...newOppForm, type: e.target.value })}
                  >
                    <option value="INTERNSHIP">Internship</option>
                    <option value="FULL_TIME">Full-Time Job</option>
                    <option value="RESEARCH">Research Fellowship</option>
                    <option value="CLINICAL_PLACEMENT">Clinical Placement</option>
                  </select>
                </label>
                <label>
                  Work Mode
                  <select
                    value={newOppForm.workMode}
                    onChange={e => setNewOppForm({ ...newOppForm, workMode: e.target.value })}
                  >
                    <option value="ON_SITE">On-Site</option>
                    <option value="HYBRID">Hybrid</option>
                    <option value="REMOTE">Remote</option>
                  </select>
                </label>
              </div>

              <label>
                Eligibility Summary
                <textarea
                  rows={2}
                  required
                  placeholder="e.g. BSc Nursing / MBBS graduates with state council registration"
                  value={newOppForm.eligibilitySummary}
                  onChange={e => setNewOppForm({ ...newOppForm, eligibilitySummary: e.target.value })}
                />
              </label>

              <label>
                Required Skills (comma separated)
                <input
                  required
                  placeholder="e.g. Clinical Trials, Medical Terminology, Patient Safety"
                  value={newOppForm.requiredSkills}
                  onChange={e => setNewOppForm({ ...newOppForm, requiredSkills: e.target.value })}
                />
              </label>

              <div className="two-col">
                <label>
                  Location
                  <input
                    value={newOppForm.location}
                    onChange={e => setNewOppForm({ ...newOppForm, location: e.target.value })}
                  />
                </label>
                <label>
                  Stipend or Salary
                  <input
                    value={newOppForm.stipendOrSalary}
                    onChange={e => setNewOppForm({ ...newOppForm, stipendOrSalary: e.target.value })}
                  />
                </label>
              </div>

              <button className="primary" disabled={busy}>
                {busy ? <LoaderCircle className="spin" size={16} /> : <CheckCircle2 size={16} />} Publish Opportunity
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

// Subcomponent: Feed Post Item
function FeedItem({ post, onReact, onComment }: { post: any; onReact: () => void; onComment: (text: string) => void }) {
  const [showCommentBox, setShowCommentBox] = useState(false);
  const [commentText, setCommentText] = useState('');

  return (
    <article className="feed-post-card panel">
      <div className="post-header">
        <span className="avatar">{post.authorAvatar || 'HC'}</span>
        <div className="grow">
          <div className="author-name-row">
            <strong>{post.authorName}</strong>
            <span className="tiny-role">{post.authorRole}</span>
          </div>
          <small className="muted">{post.authorSpecialization} · {new Date(post.createdAt).toLocaleDateString()}</small>
        </div>
        <span className="soft-tag">{post.category.replace(/_/g, ' ')}</span>
      </div>

      {post.title && <h4 className="post-title">{post.title}</h4>}
      <p className="post-body">{post.content}</p>

      {post.tags?.length > 0 && (
        <div className="post-tags-row">
          {post.tags.map((t: string) => <span key={t} className="tiny-tag">#{t}</span>)}
        </div>
      )}

      <div className="post-actions-bar">
        <button className={`like-btn ${post.likedByMe ? 'liked' : ''}`} onClick={onReact}>
          <ThumbsUp size={16} /> {post.likesCount || 0} Likes
        </button>
        <button className="comment-btn" onClick={() => setShowCommentBox(!showCommentBox)}>
          <MessageSquare size={16} /> {post.commentsCount || 0} Comments
        </button>
      </div>

      {showCommentBox && (
        <div className="comment-section mt-12">
          <div className="comment-input-row">
            <input
              placeholder="Add professional insight or question..."
              value={commentText}
              onChange={e => setCommentText(e.target.value)}
              onKeyDown={e => {
                if (e.key === 'Enter') {
                  onComment(commentText);
                  setCommentText('');
                }
              }}
            />
            <button
              className="primary small"
              onClick={() => {
                onComment(commentText);
                setCommentText('');
              }}
            >
              Post
            </button>
          </div>

          <div className="comments-stream mt-8">
            {(post.comments || []).map((c: any) => (
              <div className="comment-bubble" key={c.id}>
                <strong>{c.authorName} <small className="muted">({c.authorRole})</small>:</strong>
                <p>{c.content}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </article>
  );
}

// Subcomponent: Metric Card
function Metric({ icon: Icon, label, value, tone }: { icon: any; label: string; value: any; tone: string }) {
  return (
    <div className="metric-card">
      <div className={`metric-icon ${tone}`}><Icon size={20} /></div>
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
      </div>
      <div className="metric-decoration" />
    </div>
  );
}

// Subcomponent: Page Title
function PageTitle({ title, subtitle }: { title: string; subtitle: string }) {
  return (
    <div className="page-title">
      <span className="eyebrow">HEALTHSKILL CONNECT</span>
      <h1>{title}</h1>
      <p>{subtitle}</p>
    </div>
  );
}

// Subcomponent: Quick Action Button
function Quick({ icon: Icon, label, onClick }: { icon: any; label: string; onClick: () => void }) {
  return (
    <button className="quick-action" onClick={onClick}>
      <span><Icon size={18} /></span>
      <b>{label}</b>
      <ChevronRight size={15} />
    </button>
  );
}
