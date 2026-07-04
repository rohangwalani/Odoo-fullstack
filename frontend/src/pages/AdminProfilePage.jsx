import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, Edit2, Plus, Mail, Phone, Building2, MapPin, Briefcase, Calendar, Shield, CreditCard, Heart, User, Sparkles, X, Trash2 } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { Modal } from '../components/Modal';
import { InputField } from '../components/InputField';
import { Button } from '../components/Button';

export const AdminProfilePage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, updateUser } = useAuth();
  
  const [activeTab, setActiveTab] = useState('Overview');

  // Modal States
  const [isEditAboutOpen, setIsEditAboutOpen] = useState(false);
  const [isEditLoveOpen, setIsEditLoveOpen] = useState(false);
  const [isAddSkillOpen, setIsAddSkillOpen] = useState(false);
  const [isAddHobbyOpen, setIsAddHobbyOpen] = useState(false);
  const [isAddCertOpen, setIsAddCertOpen] = useState(false);
  const [isEditPrivateOpen, setIsEditPrivateOpen] = useState(false);
  const [isEditSalaryOpen, setIsEditSalaryOpen] = useState(false);

  // Form States
  const [aboutMeText, setAboutMeText] = useState('');
  const [loveText, setLoveText] = useState('');
  const [newSkill, setNewSkill] = useState('');
  const [newHobby, setNewHobby] = useState('');
  const [certForm, setCertForm] = useState({ name: '', org: '', issueDate: '', expiryDate: '', credentialId: '' });
  const [privateForm, setPrivateForm] = useState({ dob: '', gender: '', maritalStatus: '', nationality: '', personalEmail: '', homeAddress: '' });
  const [salaryForm, setSalaryForm] = useState(null);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const tabParam = params.get('tab');
    if (tabParam) {
      setActiveTab(tabParam);
    }
  }, [location.search]);

  if (!user) {
    return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading profile...</div>;
  }

  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  // Fallbacks mapping to user state
  const aboutMe = user.aboutMe || `Dedicated ${user.role} with extensive experience in the ${user.department || 'business'} industry. Focused on delivering high-quality results and optimizing internal processes to improve efficiency.`;
  const whatILove = user.whatILove || `"I love the ability to collaborate with brilliant minds and solve complex organizational challenges on a daily basis. The culture here empowers everyone to take ownership."`;
  const skills = user.skills || ['Leadership', 'Strategic Planning', 'Project Management', 'Communication'];
  const hobbies = user.hobbies || ['Photography', 'Hiking', 'Chess'];
  const certs = user.certifications || [
    { name: 'Advanced Agile Leadership', org: 'Scrum Alliance', issueDate: '2023' },
    { name: 'Strategic HR Management', org: 'HRCI', issueDate: '2022' }
  ];
  
  const privateInfo = user.privateInfo || {
    dob: 'Oct 12, 1985',
    gender: 'Unspecified',
    maritalStatus: 'Single',
    nationality: 'American',
    personalEmail: `personal.${user.email}`,
    homeAddress: '123 Business Avenue, Suite 400\nSan Francisco, CA 94107'
  };

  const salaryInfo = user.salaryInfo || {
    monthlyWage: 10000,
    workingDays: 5,
    breakTime: '1 Hour',
    components: {
      basic: 50,
      hra: 20,
      standardAllowance: 10,
      performanceBonus: 10,
      lta: 5,
      fixedAllowance: 5
    },
    pf: {
      employee: 12,
      employer: 12
    },
    tax: {
      professional: 200
    }
  };

  const calculateSalaryDetails = (info) => {
    const base = Number(info.monthlyWage) || 0;
    const calc = (pct) => (base * (Number(pct) / 100)).toFixed(2);
    
    const earnings = {
      basic: { pct: info.components.basic, amt: calc(info.components.basic) },
      hra: { pct: info.components.hra, amt: calc(info.components.hra) },
      standardAllowance: { pct: info.components.standardAllowance, amt: calc(info.components.standardAllowance) },
      performanceBonus: { pct: info.components.performanceBonus, amt: calc(info.components.performanceBonus) },
      lta: { pct: info.components.lta, amt: calc(info.components.lta) },
      fixedAllowance: { pct: info.components.fixedAllowance, amt: calc(info.components.fixedAllowance) },
    };
    
    const deductions = {
      pfEmployee: { pct: info.pf.employee, amt: calc(info.pf.employee) },
      pfEmployer: { pct: info.pf.employer, amt: calc(info.pf.employer) },
      profTax: { amt: Number(info.tax.professional).toFixed(2) }
    };
    
    const yearlyWage = (base * 12).toLocaleString();
    
    return { earnings, deductions, yearlyWage, base: base.toLocaleString() };
  };

  const currentSalaryCalc = calculateSalaryDetails(salaryInfo);

  // Save Handlers
  const handleSaveAbout = () => {
    updateUser({ aboutMe: aboutMeText });
    setIsEditAboutOpen(false);
  };

  const handleSaveLove = () => {
    updateUser({ whatILove: loveText });
    setIsEditLoveOpen(false);
  };

  const handleAddSkill = () => {
    if (newSkill.trim()) {
      updateUser({ skills: [...skills, newSkill.trim()] });
    }
    setNewSkill('');
    setIsAddSkillOpen(false);
  };

  const handleDeleteSkill = (skillToDelete) => {
    updateUser({ skills: skills.filter(s => s !== skillToDelete) });
  };

  const handleAddHobby = () => {
    if (newHobby.trim()) {
      updateUser({ hobbies: [...hobbies, newHobby.trim()] });
    }
    setNewHobby('');
    setIsAddHobbyOpen(false);
  };

  const handleDeleteHobby = (hobbyToDelete) => {
    updateUser({ hobbies: hobbies.filter(h => h !== hobbyToDelete) });
  };

  const handleAddCert = () => {
    if (certForm.name.trim() && certForm.org.trim()) {
      updateUser({ certifications: [...certs, certForm] });
    }
    setCertForm({ name: '', org: '', issueDate: '', expiryDate: '', credentialId: '' });
    setIsAddCertOpen(false);
  };

  const handleDeleteCert = (idxToDelete) => {
    updateUser({ certifications: certs.filter((_, idx) => idx !== idxToDelete) });
  };

  const handleSavePrivate = () => {
    updateUser({ privateInfo: privateForm });
    setIsEditPrivateOpen(false);
  };

  const handleSaveSalary = () => {
    if (salaryForm) {
      updateUser({ salaryInfo: salaryForm });
    }
    setIsEditSalaryOpen(false);
  };

  return (
    <div className="dashboard-container">
      {/* ── Header Navigation ── */}
      <header className="dashboard-header" style={{ paddingBottom: 0, borderBottom: 'none' }}>
        <div className="dashboard-brand" style={{ cursor: 'pointer' }} onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={20} style={{ marginRight: '1rem', color: 'var(--text-muted)' }} />
          <h1 className="dashboard-title">Back to Dashboard</h1>
        </div>
      </header>

      {/* ── Main Content ── */}
      <main className="dashboard-main" style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
        
        {/* Top Profile Header Card */}
        <div className="admin-profile-header">
          <div className="admin-profile-avatar" style={{ overflow: 'hidden' }}>
            {user.avatarUrl ? (
              <img src={user.avatarUrl} alt={user.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} onError={(e) => e.target.style.display = 'none'} />
            ) : null}
            <div style={{ display: user.avatarUrl ? 'none' : 'flex', width: '100%', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
              {getInitials(user.name)}
            </div>
          </div>

          <div className="admin-profile-info">
            <div className="admin-profile-title-row">
              <h2>{user.name}</h2>
              <span className={`badge badge-${user.status || 'present'}`}>{user.role}</span>
              <span className="badge" style={{ background: 'var(--bg-input)', color: 'var(--text-main)', border: '1px solid rgba(116,192,68,0.2)' }}>{user.id || 'EMP-0000'}</span>
            </div>
            
            <div className="admin-profile-contact-grid">
              <div className="contact-item"><Mail size={16} /> {user.email}</div>
              <div className="contact-item"><Phone size={16} /> {user.phone}</div>
              <div className="contact-item"><Building2 size={16} /> {user.company || 'HRIS Solutions'}</div>
              <div className="contact-item"><Briefcase size={16} /> {user.department || 'Management'}</div>
              <div className="contact-item"><User size={16} /> Manager: {user.manager || 'N/A'}</div>
              <div className="contact-item"><MapPin size={16} /> {user.location || 'Headquarters'}</div>
            </div>
          </div>

          <button className="btn btn-primary" style={{ alignSelf: 'flex-start' }}>
            <Edit2 size={16} style={{ marginRight: '0.5rem' }} /> Edit Profile
          </button>
        </div>

        {/* Tabs & Content Area */}
        <div className="profile-main-content" style={{ marginTop: '2rem' }}>
          <div className="profile-tabs-header">
            <button className={`profile-tab ${activeTab === 'Overview' ? 'active' : ''}`} onClick={() => setActiveTab('Overview')}>Overview</button>
            <button className={`profile-tab ${activeTab === 'Resume' ? 'active' : ''}`} onClick={() => setActiveTab('Resume')}>Resume</button>
            <button className={`profile-tab ${activeTab === 'Private Info' ? 'active' : ''}`} onClick={() => setActiveTab('Private Info')}>Private Info</button>
            <button className={`profile-tab ${activeTab === 'Salary Info' ? 'active' : ''}`} onClick={() => setActiveTab('Salary Info')}>Salary Info</button>
            <button className={`profile-tab ${activeTab === 'Attendance' ? 'active' : ''}`} onClick={() => setActiveTab('Attendance')}>My Attendance</button>
          </div>

          <div className="profile-tab-content">
            
            {/* ── RESUME TAB ── */}
            {(activeTab === 'Resume' || activeTab === 'Overview') && (
              <div className="tab-pane admin-resume-grid" style={{ marginBottom: activeTab === 'Overview' ? '3rem' : '0' }}>
                
                {/* About & Why I Love My Job (Left Col) */}
                <div className="admin-resume-col">
                  <div className="content-card">
                    <div className="content-card-header">
                      <h3>About Me</h3>
                      <button className="icon-btn" onClick={() => { setAboutMeText(aboutMe); setIsEditAboutOpen(true); }}><Edit2 size={16}/></button>
                    </div>
                    <p style={{ whiteSpace: 'pre-line' }}>{aboutMe}</p>
                  </div>

                  <div className="content-card" style={{ background: 'rgba(22, 140, 140, 0.03)', border: '1px solid rgba(22, 140, 140, 0.1)' }}>
                    <div className="content-card-header">
                      <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--accent-hover)' }}><Heart size={18}/> What I love about my job</h3>
                      <button className="icon-btn" onClick={() => { setLoveText(whatILove); setIsEditLoveOpen(true); }}><Edit2 size={16}/></button>
                    </div>
                    <p style={{ color: 'var(--text-main)', fontStyle: 'italic', whiteSpace: 'pre-line' }}>{whatILove}</p>
                  </div>
                </div>

                {/* Skills, Certs, Hobbies (Right Col) */}
                <div className="admin-resume-col">
                  <div className="content-card">
                    <div className="content-card-header">
                      <h3>Skills</h3>
                      <button className="icon-btn" onClick={() => setIsAddSkillOpen(true)}><Plus size={16}/></button>
                    </div>
                    <div className="flex-tags">
                      {skills.map((skill, idx) => (
                        <span key={idx} className="skill-badge">
                          {skill}
                          <button className="tag-delete-btn" onClick={() => handleDeleteSkill(skill)}><X size={12}/></button>
                        </span>
                      ))}
                      {skills.length === 0 && <span style={{ color: 'var(--text-muted)' }}>No skills added.</span>}
                    </div>
                  </div>

                  <div className="content-card">
                    <div className="content-card-header">
                      <h3>Interests & Hobbies</h3>
                      <button className="icon-btn" onClick={() => setIsAddHobbyOpen(true)}><Plus size={16}/></button>
                    </div>
                    <div className="flex-tags">
                      {hobbies.map((hobby, idx) => (
                        <span key={idx} className="hobby-badge">
                          {hobby}
                          <button className="tag-delete-btn" onClick={() => handleDeleteHobby(hobby)}><X size={12}/></button>
                        </span>
                      ))}
                      {hobbies.length === 0 && <span style={{ color: 'var(--text-muted)' }}>No hobbies added.</span>}
                    </div>
                  </div>

                  <div className="content-card">
                    <div className="content-card-header">
                      <h3>Certifications</h3>
                      <button className="icon-btn" onClick={() => setIsAddCertOpen(true)}><Plus size={16}/></button>
                    </div>
                    <ul className="cert-list">
                      {certs.map((cert, idx) => (
                        <li key={idx}>
                          <Sparkles size={16} color="var(--accent-primary)" />
                          <div style={{ flex: 1 }}>
                            <strong>{cert.name}</strong>
                            <span>
                              {cert.org} {cert.issueDate && `• Issued ${cert.issueDate}`} {cert.credentialId && `• ID: ${cert.credentialId}`}
                            </span>
                          </div>
                          <button className="cert-delete-btn" onClick={() => handleDeleteCert(idx)}><Trash2 size={14}/></button>
                        </li>
                      ))}
                      {certs.length === 0 && <li style={{ color: 'var(--text-muted)' }}>No certifications added.</li>}
                    </ul>
                  </div>
                </div>

              </div>
            )}

            {/* ── PRIVATE INFO TAB ── */}
            {(activeTab === 'Private Info' || activeTab === 'Overview') && (
              <div className="tab-pane" style={{ marginBottom: activeTab === 'Overview' ? '3rem' : '0', paddingTop: activeTab === 'Overview' ? '3rem' : '0', borderTop: activeTab === 'Overview' ? '1px solid rgba(0,0,0,0.05)' : 'none' }}>
                <div className="content-card-header" style={{ marginBottom: '1.5rem' }}>
                  <h3 className="pane-title" style={{ margin: 0 }}>Private Information</h3>
                  <button className="btn btn-sm" onClick={() => { setPrivateForm(privateInfo); setIsEditPrivateOpen(true); }}><Edit2 size={14} style={{ marginRight: '0.5rem' }}/> Edit Details</button>
                </div>
                
                <div className="admin-detail-grid">
                  <div className="detail-box">
                    <span className="box-label">Date of Birth</span>
                    <span className="box-value">{privateInfo.dob}</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Gender</span>
                    <span className="box-value">{privateInfo.gender}</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Marital Status</span>
                    <span className="box-value">{privateInfo.maritalStatus}</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Nationality</span>
                    <span className="box-value">{privateInfo.nationality}</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Personal Email</span>
                    <span className="box-value">{privateInfo.personalEmail}</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Home Address</span>
                    <span className="box-value" style={{ whiteSpace: 'pre-line' }}>{privateInfo.homeAddress}</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Joining Date</span>
                    <span className="box-value">{user.joiningDate || 'Jan 10, 2022'}</span>
                  </div>
                </div>

                <h3 className="pane-title" style={{ margin: '2.5rem 0 1.5rem 0' }}>Bank & Legal Details</h3>
                <div className="admin-detail-grid">
                  <div className="detail-box">
                    <span className="box-label">Bank Name</span>
                    <span className="box-value">Chase Bank</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Account Number</span>
                    <span className="box-value">**** **** 5678</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Routing Number</span>
                    <span className="box-value">122000248</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Social Security No.</span>
                    <span className="box-value">XXX-XX-1234</span>
                  </div>
                </div>
              </div>
            )}

            {/* ── SALARY INFO TAB ── */}
            {(activeTab === 'Salary Info' || activeTab === 'Overview') && (
              <div className="tab-pane" style={{ marginBottom: activeTab === 'Overview' ? '3rem' : '0', paddingTop: activeTab === 'Overview' ? '3rem' : '0', borderTop: activeTab === 'Overview' ? '1px solid rgba(0,0,0,0.05)' : 'none' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                  <h3 className="pane-title" style={{ margin: 0 }}>Salary Structure & Compensation</h3>
                  {activeTab === 'Salary Info' && (
                    <button className="btn btn-outline btn-sm" onClick={() => { setSalaryForm(JSON.parse(JSON.stringify(salaryInfo))); setIsEditSalaryOpen(true); }}>
                      <Edit2 size={14} style={{ marginRight: '0.5rem' }}/> Edit Salary Details
                    </button>
                  )}
                </div>
                
                <div className="admin-salary-hero" style={{ display: 'flex', gap: '2rem', padding: '1.5rem', background: 'var(--bg-card)', border: '1px solid rgba(0,0,0,0.05)', borderRadius: '12px' }}>
                  <div className="salary-hero-main" style={{ flex: 1 }}>
                    <span className="box-label">Monthly Wage</span>
                    <span className="salary-hero-value" style={{ display: 'block', fontSize: '2rem', fontWeight: 'bold', color: 'var(--accent-primary)', marginBottom: '0.25rem' }}>${currentSalaryCalc.base}</span>
                    <span className="salary-hero-sub" style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>Working Days: {salaryInfo.workingDays} days / week</span>
                  </div>
                  <div className="salary-hero-main" style={{ flex: 1, borderLeft: '1px solid rgba(0,0,0,0.05)', paddingLeft: '2rem' }}>
                    <span className="box-label">Yearly Wage</span>
                    <span className="salary-hero-value" style={{ display: 'block', fontSize: '2rem', fontWeight: 'bold', color: 'var(--text-main)', marginBottom: '0.25rem' }}>${currentSalaryCalc.yearlyWage}</span>
                    <span className="salary-hero-sub" style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>Break Time: {salaryInfo.breakTime}</span>
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '2rem', marginTop: '2rem' }}>
                  
                  {/* Earnings */}
                  <div>
                    <h4 style={{ margin: '0 0 1rem 0', color: 'var(--text-main)', fontSize: '1.1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', paddingBottom: '0.5rem' }}>Salary Components</h4>
                    <div className="admin-detail-grid">
                      <div className="detail-box">
                        <span className="box-label">Basic Salary ({currentSalaryCalc.earnings.basic.pct}%)</span>
                        <span className="box-value">${currentSalaryCalc.earnings.basic.amt}</span>
                      </div>
                      <div className="detail-box">
                        <span className="box-label">House Rent Allowance ({currentSalaryCalc.earnings.hra.pct}%)</span>
                        <span className="box-value">${currentSalaryCalc.earnings.hra.amt}</span>
                      </div>
                      <div className="detail-box">
                        <span className="box-label">Standard Allowance ({currentSalaryCalc.earnings.standardAllowance.pct}%)</span>
                        <span className="box-value">${currentSalaryCalc.earnings.standardAllowance.amt}</span>
                      </div>
                      <div className="detail-box">
                        <span className="box-label">Performance Bonus ({currentSalaryCalc.earnings.performanceBonus.pct}%)</span>
                        <span className="box-value">${currentSalaryCalc.earnings.performanceBonus.amt}</span>
                      </div>
                      <div className="detail-box">
                        <span className="box-label">Leave Travel Allowance ({currentSalaryCalc.earnings.lta.pct}%)</span>
                        <span className="box-value">${currentSalaryCalc.earnings.lta.amt}</span>
                      </div>
                      <div className="detail-box">
                        <span className="box-label">Fixed Allowance ({currentSalaryCalc.earnings.fixedAllowance.pct}%)</span>
                        <span className="box-value">${currentSalaryCalc.earnings.fixedAllowance.amt}</span>
                      </div>
                    </div>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
                    {/* PF */}
                    <div>
                      <h4 style={{ margin: '0 0 1rem 0', color: 'var(--text-main)', fontSize: '1.1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', paddingBottom: '0.5rem' }}>PF Contribution</h4>
                      <div className="admin-detail-grid" style={{ gridTemplateColumns: '1fr' }}>
                        <div className="detail-box">
                          <span className="box-label">Employee PF ({currentSalaryCalc.deductions.pfEmployee.pct}%)</span>
                          <span className="box-value text-danger">-${currentSalaryCalc.deductions.pfEmployee.amt}</span>
                        </div>
                        <div className="detail-box">
                          <span className="box-label">Employer PF ({currentSalaryCalc.deductions.pfEmployer.pct}%)</span>
                          <span className="box-value">${currentSalaryCalc.deductions.pfEmployer.amt}</span>
                        </div>
                      </div>
                    </div>
                    {/* Taxes */}
                    <div>
                      <h4 style={{ margin: '0 0 1rem 0', color: 'var(--text-main)', fontSize: '1.1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', paddingBottom: '0.5rem' }}>Tax Deductions</h4>
                      <div className="admin-detail-grid" style={{ gridTemplateColumns: '1fr' }}>
                        <div className="detail-box">
                          <span className="box-label">Professional Tax (Fixed)</span>
                          <span className="box-value text-danger">-${currentSalaryCalc.deductions.profTax.amt}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                </div>
              </div>
            )}

            {/* ── ATTENDANCE TAB ── */}
            {(activeTab === 'Attendance' || activeTab === 'Overview') && (
              <div className="tab-pane" style={{ paddingTop: activeTab === 'Overview' ? '3rem' : '0', borderTop: activeTab === 'Overview' ? '1px solid rgba(0,0,0,0.05)' : 'none' }}>
                <h3 className="pane-title">My Attendance & Leaves</h3>
                <div className="admin-detail-grid">
                  <div className="detail-box">
                    <span className="box-label">Leaves Taken (YTD)</span>
                    <span className="box-value">4 Days</span>
                  </div>
                  <div className="detail-box">
                    <span className="box-label">Leaves Remaining</span>
                    <span className="box-value">16 Days</span>
                  </div>
                </div>
              </div>
            )}

          </div>
        </div>
      </main>

      {/* MODALS */}

      {/* Edit About Me */}
      <Modal isOpen={isEditAboutOpen} onClose={() => setIsEditAboutOpen(false)} title="Edit About Me">
        <div className="field-group">
          <textarea 
            className="input-field" 
            style={{ padding: '1rem', height: '120px', resize: 'vertical' }}
            value={aboutMeText} 
            onChange={(e) => setAboutMeText(e.target.value)}
            placeholder="Write something about yourself..."
          />
        </div>
        <Button onClick={handleSaveAbout}>Save Changes</Button>
      </Modal>

      {/* Edit What I Love */}
      <Modal isOpen={isEditLoveOpen} onClose={() => setIsEditLoveOpen(false)} title="What I love about my job">
        <div className="field-group">
          <textarea 
            className="input-field" 
            style={{ padding: '1rem', height: '120px', resize: 'vertical' }}
            value={loveText} 
            onChange={(e) => setLoveText(e.target.value)}
            placeholder="Share what excites you about your role..."
          />
        </div>
        <Button onClick={handleSaveLove}>Save Changes</Button>
      </Modal>

      {/* Add Skill */}
      <Modal isOpen={isAddSkillOpen} onClose={() => setIsAddSkillOpen(false)} title="Add Skill">
        <div className="field-group">
          <input 
            type="text" 
            className="input-field" 
            style={{ padding: '0 1rem' }}
            value={newSkill} 
            onChange={(e) => setNewSkill(e.target.value)}
            placeholder="e.g. React.js, Public Speaking"
            onKeyDown={(e) => e.key === 'Enter' && handleAddSkill()}
          />
        </div>
        <Button onClick={handleAddSkill}>Add Skill</Button>
      </Modal>

      {/* Add Hobby */}
      <Modal isOpen={isAddHobbyOpen} onClose={() => setIsAddHobbyOpen(false)} title="Add Hobby">
        <div className="field-group">
          <input 
            type="text" 
            className="input-field" 
            style={{ padding: '0 1rem' }}
            value={newHobby} 
            onChange={(e) => setNewHobby(e.target.value)}
            placeholder="e.g. Photography, Hiking"
            onKeyDown={(e) => e.key === 'Enter' && handleAddHobby()}
          />
        </div>
        <Button onClick={handleAddHobby}>Add Hobby</Button>
      </Modal>

      {/* Add Certification */}
      <Modal isOpen={isAddCertOpen} onClose={() => setIsAddCertOpen(false)} title="Add Certification">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="field-group">
            <label className="field-label">Certification Name *</label>
            <input type="text" className="input-field" style={{ padding: '0 1rem' }} value={certForm.name} onChange={e => setCertForm({...certForm, name: e.target.value})} />
          </div>
          <div className="field-group">
            <label className="field-label">Issuing Organization *</label>
            <input type="text" className="input-field" style={{ padding: '0 1rem' }} value={certForm.org} onChange={e => setCertForm({...certForm, org: e.target.value})} />
          </div>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <div className="field-group" style={{ flex: 1 }}>
              <label className="field-label">Issue Date</label>
              <input type="text" className="input-field" style={{ padding: '0 1rem' }} placeholder="e.g. 2023" value={certForm.issueDate} onChange={e => setCertForm({...certForm, issueDate: e.target.value})} />
            </div>
            <div className="field-group" style={{ flex: 1 }}>
              <label className="field-label">Expiry Date</label>
              <input type="text" className="input-field" style={{ padding: '0 1rem' }} placeholder="e.g. 2025" value={certForm.expiryDate} onChange={e => setCertForm({...certForm, expiryDate: e.target.value})} />
            </div>
          </div>
          <div className="field-group">
            <label className="field-label">Credential ID</label>
            <input type="text" className="input-field" style={{ padding: '0 1rem' }} value={certForm.credentialId} onChange={e => setCertForm({...certForm, credentialId: e.target.value})} />
          </div>
        </div>
        <Button onClick={handleAddCert} className="mt-2">Add Certification</Button>
      </Modal>

      {/* Edit Private Info */}
      <Modal isOpen={isEditPrivateOpen} onClose={() => setIsEditPrivateOpen(false)} title="Edit Private Information">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', maxHeight: '60vh', overflowY: 'auto', paddingRight: '0.5rem' }}>
          <div className="field-group">
            <label className="field-label">Date of Birth</label>
            <input type="text" className="input-field" style={{ padding: '0 1rem' }} value={privateForm.dob} onChange={e => setPrivateForm({...privateForm, dob: e.target.value})} />
          </div>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <div className="field-group" style={{ flex: 1 }}>
              <label className="field-label">Gender</label>
              <input type="text" className="input-field" style={{ padding: '0 1rem' }} value={privateForm.gender} onChange={e => setPrivateForm({...privateForm, gender: e.target.value})} />
            </div>
            <div className="field-group" style={{ flex: 1 }}>
              <label className="field-label">Marital Status</label>
              <input type="text" className="input-field" style={{ padding: '0 1rem' }} value={privateForm.maritalStatus} onChange={e => setPrivateForm({...privateForm, maritalStatus: e.target.value})} />
            </div>
          </div>
          <div className="field-group">
            <label className="field-label">Nationality</label>
            <input type="text" className="input-field" style={{ padding: '0 1rem' }} value={privateForm.nationality} onChange={e => setPrivateForm({...privateForm, nationality: e.target.value})} />
          </div>
          <div className="field-group">
            <label className="field-label">Personal Email</label>
            <input type="email" className="input-field" style={{ padding: '0 1rem' }} value={privateForm.personalEmail} onChange={e => setPrivateForm({...privateForm, personalEmail: e.target.value})} />
          </div>
          <div className="field-group">
            <label className="field-label">Home Address</label>
            <textarea className="input-field" style={{ padding: '1rem', height: '80px', resize: 'vertical' }} value={privateForm.homeAddress} onChange={e => setPrivateForm({...privateForm, homeAddress: e.target.value})} />
          </div>
        </div>
        <Button onClick={handleSavePrivate} className="mt-2">Save Changes</Button>
      </Modal>

      {/* Edit Salary Modal */}
      {salaryForm && (
        <Modal isOpen={isEditSalaryOpen} onClose={() => setIsEditSalaryOpen(false)} title="Edit Salary Details">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', maxHeight: '65vh', overflowY: 'auto', paddingRight: '0.5rem' }}>
            
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="field-group">
                <label className="field-label">Monthly Wage ($)</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.monthlyWage} onChange={e => setSalaryForm({...salaryForm, monthlyWage: e.target.value})} />
              </div>
              <div className="field-group">
                <label className="field-label">Working Days (per week)</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.workingDays} onChange={e => setSalaryForm({...salaryForm, workingDays: e.target.value})} />
              </div>
              <div className="field-group">
                <label className="field-label">Break Time</label>
                <input type="text" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.breakTime} onChange={e => setSalaryForm({...salaryForm, breakTime: e.target.value})} />
              </div>
            </div>

            <h4 style={{ margin: '0.5rem 0 0 0', fontSize: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', paddingBottom: '0.5rem' }}>Component Percentages (%)</h4>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="field-group">
                <label className="field-label">Basic Salary %</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.components.basic} onChange={e => setSalaryForm({...salaryForm, components: {...salaryForm.components, basic: e.target.value}})} />
              </div>
              <div className="field-group">
                <label className="field-label">HRA %</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.components.hra} onChange={e => setSalaryForm({...salaryForm, components: {...salaryForm.components, hra: e.target.value}})} />
              </div>
              <div className="field-group">
                <label className="field-label">Standard Allowance %</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.components.standardAllowance} onChange={e => setSalaryForm({...salaryForm, components: {...salaryForm.components, standardAllowance: e.target.value}})} />
              </div>
              <div className="field-group">
                <label className="field-label">Performance Bonus %</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.components.performanceBonus} onChange={e => setSalaryForm({...salaryForm, components: {...salaryForm.components, performanceBonus: e.target.value}})} />
              </div>
              <div className="field-group">
                <label className="field-label">LTA %</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.components.lta} onChange={e => setSalaryForm({...salaryForm, components: {...salaryForm.components, lta: e.target.value}})} />
              </div>
              <div className="field-group">
                <label className="field-label">Fixed Allowance %</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.components.fixedAllowance} onChange={e => setSalaryForm({...salaryForm, components: {...salaryForm.components, fixedAllowance: e.target.value}})} />
              </div>
            </div>

            <h4 style={{ margin: '0.5rem 0 0 0', fontSize: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', paddingBottom: '0.5rem' }}>PF & Taxes</h4>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="field-group">
                <label className="field-label">Employee PF %</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.pf.employee} onChange={e => setSalaryForm({...salaryForm, pf: {...salaryForm.pf, employee: e.target.value}})} />
              </div>
              <div className="field-group">
                <label className="field-label">Employer PF %</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.pf.employer} onChange={e => setSalaryForm({...salaryForm, pf: {...salaryForm.pf, employer: e.target.value}})} />
              </div>
              <div className="field-group" style={{ gridColumn: '1 / -1' }}>
                <label className="field-label">Professional Tax (Fixed $)</label>
                <input type="number" className="input-field" style={{ padding: '0 1rem' }} value={salaryForm.tax.professional} onChange={e => setSalaryForm({...salaryForm, tax: {...salaryForm.tax, professional: e.target.value}})} />
              </div>
            </div>

          </div>
          <Button onClick={handleSaveSalary} className="mt-4" style={{ width: '100%' }}>Save Salary Details</Button>
        </Modal>
      )}

    </div>
  );
};
