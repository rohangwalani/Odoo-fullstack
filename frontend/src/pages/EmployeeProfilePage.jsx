import { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import {
  ArrowLeft,
  Briefcase,
  Mail,
  Phone,
  MapPin,
  Shield,
  Edit2
} from 'lucide-react';

import { useAuth } from '../hooks/useAuth';
import { getEmployeeById } from '../data/mockEmployees';
import { Modal } from '../components/Modal';
import { Button } from '../components/Button';

export const EmployeeProfilePage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user, updateEmployee } = useAuth();

  const [employee, setEmployee] = useState(null);
  const [activeTab, setActiveTab] = useState('Overview');
  const [isEditSalaryOpen, setIsEditSalaryOpen] = useState(false);
  const [salaryForm, setSalaryForm] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const tabParam = params.get('tab');

    if (tabParam) {
      setActiveTab(tabParam);
    }
  }, [location.search]);

  useEffect(() => {
    const fetchEmployee = async () => {
      try {
        const axiosInstance = (await import('../api/axiosInstance')).default;
        const response = await axiosInstance.get(`/employees/${id}`);
        // Map backend employee to frontend format
        const empData = response.data;
        setEmployee({
          ...empData,
          name: `${empData.firstName} ${empData.lastName}`,
          empId: empData.loginId,
          avatarUrl: empData.profilePicture ? `http://localhost:8080/uploads/${empData.profilePicture}` : null,
          status: 'present' // Mocked status since not in API
        });
      } catch (e) {
        console.error('Failed to load employee:', e);
        navigate('/dashboard');
      } finally {
        setLoading(false);
      }
    };
    fetchEmployee();
  }, [id, navigate]);

  if (loading || !employee) {
    return <div style={{ padding: '2rem' }}>Loading...</div>;
  }

  const getInitials = (name) => {
    if (!name) return 'U';

    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .substring(0, 2)
      .toUpperCase();
  };

  const salaryInfo = employee.salaryInfo || {
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

    const calc = (pct) =>
      (base * (Number(pct) / 100)).toFixed(2);

    const earnings = {
      basic: {
        pct: info.components.basic,
        amt: calc(info.components.basic)
      },
      hra: {
        pct: info.components.hra,
        amt: calc(info.components.hra)
      },
      standardAllowance: {
        pct: info.components.standardAllowance,
        amt: calc(info.components.standardAllowance)
      },
      performanceBonus: {
        pct: info.components.performanceBonus,
        amt: calc(info.components.performanceBonus)
      },
      lta: {
        pct: info.components.lta,
        amt: calc(info.components.lta)
      },
      fixedAllowance: {
        pct: info.components.fixedAllowance,
        amt: calc(info.components.fixedAllowance)
      }
    };

    const deductions = {
      pfEmployee: {
        pct: info.pf.employee,
        amt: calc(info.pf.employee)
      },
      pfEmployer: {
        pct: info.pf.employer,
        amt: calc(info.pf.employer)
      },
      profTax: {
        amt: Number(info.tax.professional || 0).toFixed(2)
      }
    };

    return {
      earnings,
      deductions,
      yearlyWage: (base * 12).toLocaleString('en-IN'),
      base: base.toLocaleString('en-IN')
    };
  };

  const currentSalaryCalc = calculateSalaryDetails(salaryInfo);

  const canViewPrivateInfo =
    user?.role === 'Admin' ||
    user?.role === 'HR Officer' ||
    String(user?.id) === String(employee.id);

  const canViewSalaryInfo =
    user?.role === 'Admin' ||
    user?.role === 'HR Officer' ||
    String(user?.id) === String(employee.id);

  const handleSaveSalary = () => {
    if (salaryForm) {
      const updatedEmp = updateEmployee?.(employee.id, {
        salaryInfo: salaryForm
      });

      if (updatedEmp) {
        setEmployee(updatedEmp);
      } else {
        setEmployee((prev) => ({
          ...prev,
          salaryInfo: salaryForm
        }));
      }
    }

    setIsEditSalaryOpen(false);
  };

  return (
    <>
      <div className="dashboard-container">
        {/* Header */}
        <header className="dashboard-header">
          <div
            className="dashboard-brand"
            style={{ cursor: 'pointer' }}
            onClick={() => navigate('/dashboard')}
          >
            <ArrowLeft
              size={20}
              style={{
                marginRight: '1rem',
                color: 'var(--text-muted)'
              }}
            />

            <h1 className="dashboard-title">
              Back to Directory
            </h1>
          </div>
        </header>

        {/* Main Content */}
        <main
          className="dashboard-main"
          style={{
            display: 'flex',
            gap: '2rem',
            padding: '2rem',
            maxWidth: '1200px',
            margin: '0 auto'
          }}
        >
          {/* Left Sidebar */}
          <aside className="profile-sidebar">
            <div className="profile-card">
              <div
                className="profile-card-avatar"
                style={{ overflow: 'hidden' }}
              >
                {employee.avatarUrl ? (
                  <img
                    src={employee.avatarUrl}
                    alt={employee.name}
                    style={{
                      width: '100%',
                      height: '100%',
                      objectFit: 'cover'
                    }}
                    onError={(e) => {
                      e.currentTarget.style.display = 'none';

                      const fallback =
                        e.currentTarget.nextElementSibling;

                      if (fallback) {
                        fallback.style.display = 'flex';
                      }
                    }}
                  />
                ) : null}

                <div
                  style={{
                    display: employee.avatarUrl ? 'none' : 'flex',
                    width: '100%',
                    height: '100%',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}
                >
                  {getInitials(employee.name)}
                </div>
              </div>

              <h2>{employee.name}</h2>

              <p className="profile-card-role">
                {employee.role}
              </p>

              <div
                className={`badge badge-${employee.status}`}
                style={{ margin: '1rem auto' }}
              >
                {employee.status || 'Unknown'}
              </div>

              <div className="profile-card-contacts">
                <div className="contact-item">
                  <Briefcase size={16} />
                  <span>{employee.department || '-'}</span>
                </div>

                <div className="contact-item">
                  <Mail size={16} />
                  <span>{employee.email || '-'}</span>
                </div>

                <div className="contact-item">
                  <Phone size={16} />
                  <span>{employee.phone || '-'}</span>
                </div>

                <div className="contact-item">
                  <MapPin size={16} />
                  <span>{employee.location || '-'}</span>
                </div>
              </div>

              {(user?.role === 'Admin' ||
                user?.role === 'HR Officer') && (
                  <button
                    className="btn btn-primary"
                    style={{
                      width: '100%',
                      marginTop: '1.5rem'
                    }}
                  >
                    Edit Employee
                  </button>
                )}
            </div>
          </aside>

          {/* Right Content */}
          <div className="profile-main-content">
            {/* Tabs */}
            <div className="profile-tabs-header">
              <button
                className={`profile-tab ${activeTab === 'Overview' ? 'active' : ''
                  }`}
                onClick={() => setActiveTab('Overview')}
              >
                Overview
              </button>

              <button
                className={`profile-tab ${activeTab === 'Attendance' ? 'active' : ''
                  }`}
                onClick={() => setActiveTab('Attendance')}
              >
                Attendance
              </button>

              <button
                className={`profile-tab ${activeTab === 'Resume' ? 'active' : ''
                  }`}
                onClick={() => setActiveTab('Resume')}
              >
                Resume
              </button>

              {canViewPrivateInfo && (
                <button
                  className={`profile-tab ${activeTab === 'Private Info' ? 'active' : ''
                    }`}
                  onClick={() => setActiveTab('Private Info')}
                >
                  Private Info
                </button>
              )}

              {canViewSalaryInfo && (
                <button
                  className={`profile-tab ${activeTab === 'Salary Info' ? 'active' : ''
                    }`}
                  onClick={() => setActiveTab('Salary Info')}
                >
                  Salary Info
                </button>
              )}
            </div>

            {/* Tab Content */}
            <div className="profile-tab-content">
              {/* Overview */}
              {activeTab === 'Overview' && (
                <div className="tab-pane">
                  <h3 className="pane-title">
                    Professional Overview
                  </h3>

                  <div className="detail-grid">
                    <div className="detail-box">
                      <span className="box-label">
                        Employee ID
                      </span>
                      <span className="box-value">
                        {employee.empId || '-'}
                      </span>
                    </div>

                    <div className="detail-box">
                      <span className="box-label">
                        Manager
                      </span>
                      <span className="box-value">
                        {employee.manager || '-'}
                      </span>
                    </div>

                    <div className="detail-box">
                      <span className="box-label">
                        Joining Date
                      </span>
                      <span className="box-value">
                        {employee.joiningDate || '-'}
                      </span>
                    </div>

                    <div className="detail-box">
                      <span className="box-label">
                        Employment Type
                      </span>
                      <span className="box-value">
                        {employee.employmentType || 'Full-Time'}
                      </span>
                    </div>
                  </div>
                </div>
              )}

              {/* Attendance */}
              {activeTab === 'Attendance' && (
                <div className="tab-pane">
                  <h3 className="pane-title">
                    Attendance & Time Off
                  </h3>

                  <div className="detail-grid">
                    <div className="detail-box">
                      <span className="box-label">
                        Leaves Taken (YTD)
                      </span>
                      <span className="box-value">
                        4 Days
                      </span>
                    </div>

                    <div className="detail-box">
                      <span className="box-label">
                        Leaves Remaining
                      </span>
                      <span className="box-value">
                        16 Days
                      </span>
                    </div>
                  </div>
                </div>
              )}

              {/* Resume */}
              {activeTab === 'Resume' && (
                <div className="tab-pane">
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      marginBottom: '1.5rem',
                      gap: '1rem',
                      flexWrap: 'wrap'
                    }}
                  >
                    <h3
                      className="pane-title"
                      style={{ margin: 0 }}
                    >
                      Resume & Skills
                    </h3>

                    {employee.resumeUrl && (
                      <a
                        href={employee.resumeUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm"
                      >
                        Open in New Tab
                      </a>
                    )}
                  </div>

                  <div
                    className="resume-section"
                    style={{
                      display: 'grid',
                      gridTemplateColumns: '1fr 2fr',
                      gap: '2rem'
                    }}
                  >
                    <div>
                      <h4>About</h4>

                      <p>
                        Dedicated {employee.role || 'employee'} with
                        extensive experience in the{' '}
                        {employee.department || 'professional'} industry.
                      </p>

                      <h4>Skills</h4>

                      <div
                        style={{
                          display: 'flex',
                          flexWrap: 'wrap',
                          gap: '0.5rem',
                          marginTop: '0.5rem'
                        }}
                      >
                        <span className="skill-badge">
                          Project Management
                        </span>
                        <span className="skill-badge">
                          Communication
                        </span>
                        <span className="skill-badge">
                          Leadership
                        </span>
                        <span className="skill-badge">
                          Agile
                        </span>
                      </div>
                    </div>

                    <div
                      style={{
                        height: '600px',
                        border:
                          '1px solid rgba(116, 192, 68, 0.2)',
                        borderRadius: '8px',
                        overflow: 'hidden',
                        background: '#f5f5f5',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                    >
                      {employee.resumeUrl ? (
                        <img
                          src={employee.resumeUrl}
                          alt="Resume Document"
                          style={{
                            width: '100%',
                            height: '100%',
                            objectFit: 'cover'
                          }}
                        />
                      ) : (
                        <span
                          style={{
                            color: 'var(--text-muted)'
                          }}
                        >
                          No resume uploaded
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              )}

              {/* Private Info */}
              {canViewPrivateInfo &&
                activeTab === 'Private Info' && (
                  <div className="tab-pane">
                    <h3 className="pane-title">
                      Private Information
                    </h3>

                    <p className="restricted-warning">
                      <Shield
                        size={14}
                        style={{ marginRight: '0.25rem' }}
                      />
                      Restricted Access
                    </p>

                    <div
                      className="detail-grid"
                      style={{ marginTop: '1rem' }}
                    >
                      <div className="detail-box">
                        <span className="box-label">
                          Social Security Number
                        </span>
                        <span className="box-value">
                          XXX-XX-1234
                        </span>
                      </div>

                      <div className="detail-box">
                        <span className="box-label">
                          Emergency Contact
                        </span>
                        <span className="box-value">
                          +1 999-888-7777
                        </span>
                      </div>
                    </div>
                  </div>
                )}

              {/* Salary Info */}
              {canViewSalaryInfo &&
                activeTab === 'Salary Info' && (
                  <div className="tab-pane">
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        marginBottom: '1.5rem',
                        gap: '1rem',
                        flexWrap: 'wrap'
                      }}
                    >
                      <h3
                        className="pane-title"
                        style={{ margin: 0 }}
                      >
                        Salary Structure & Compensation
                      </h3>

                      <div
                        style={{
                          display: 'flex',
                          gap: '1rem',
                          alignItems: 'center',
                          flexWrap: 'wrap'
                        }}
                      >
                        <p
                          className="restricted-warning"
                          style={{ margin: 0 }}
                        >
                          <Shield
                            size={14}
                            style={{ marginRight: '0.25rem' }}
                          />
                          Restricted Access
                        </p>

                        {user?.role === 'Admin' && (
                          <button
                            className="btn btn-outline btn-sm"
                            onClick={() => {
                              setSalaryForm(
                                JSON.parse(
                                  JSON.stringify(salaryInfo)
                                )
                              );
                              setIsEditSalaryOpen(true);
                            }}
                          >
                            <Edit2
                              size={14}
                              style={{ marginRight: '0.5rem' }}
                            />
                            Edit Salary Details
                          </button>
                        )}
                      </div>
                    </div>

                    {/* Salary Summary */}
                    <div
                      className="admin-salary-hero"
                      style={{
                        display: 'flex',
                        gap: '2rem',
                        padding: '1.5rem',
                        background: 'var(--bg-card)',
                        border: '1px solid rgba(0,0,0,0.05)',
                        borderRadius: '12px'
                      }}
                    >
                      <div
                        className="salary-hero-main"
                        style={{ flex: 1 }}
                      >
                        <span className="box-label">
                          Monthly Wage
                        </span>

                        <span
                          className="salary-hero-value"
                          style={{
                            display: 'block',
                            fontSize: '2rem',
                            fontWeight: 'bold',
                            color: 'var(--accent-primary)',
                            marginBottom: '0.25rem'
                          }}
                        >
                          ₹{currentSalaryCalc.base}
                        </span>

                        <span
                          className="salary-hero-sub"
                          style={{
                            fontSize: '0.9rem',
                            color: 'var(--text-muted)'
                          }}
                        >
                          Working Days: {salaryInfo.workingDays}{' '}
                          days / week
                        </span>
                      </div>

                      <div
                        className="salary-hero-main"
                        style={{
                          flex: 1,
                          borderLeft:
                            '1px solid rgba(0,0,0,0.05)',
                          paddingLeft: '2rem'
                        }}
                      >
                        <span className="box-label">
                          Yearly Wage
                        </span>

                        <span
                          className="salary-hero-value"
                          style={{
                            display: 'block',
                            fontSize: '2rem',
                            fontWeight: 'bold',
                            color: 'var(--text-main)',
                            marginBottom: '0.25rem'
                          }}
                        >
                          ₹{currentSalaryCalc.yearlyWage}
                        </span>

                        <span
                          className="salary-hero-sub"
                          style={{
                            fontSize: '0.9rem',
                            color: 'var(--text-muted)'
                          }}
                        >
                          Break Time: {salaryInfo.breakTime}
                        </span>
                      </div>
                    </div>

                    {/* Salary Components */}
                    <div
                      style={{
                        display: 'grid',
                        gridTemplateColumns: '1fr',
                        gap: '2rem',
                        marginTop: '2rem'
                      }}
                    >
                      <div>
                        <h4
                          style={{
                            margin: '0 0 1rem 0',
                            color: 'var(--text-main)',
                            fontSize: '1.1rem',
                            borderBottom:
                              '1px solid rgba(0,0,0,0.05)',
                            paddingBottom: '0.5rem'
                          }}
                        >
                          Salary Components
                        </h4>

                        <div className="admin-detail-grid">
                          <div className="detail-box">
                            <span className="box-label">
                              Basic Salary (
                              {currentSalaryCalc.earnings.basic.pct}%)
                            </span>
                            <span className="box-value">
                              ₹{currentSalaryCalc.earnings.basic.amt}
                            </span>
                          </div>

                          <div className="detail-box">
                            <span className="box-label">
                              House Rent Allowance (
                              {currentSalaryCalc.earnings.hra.pct}%)
                            </span>
                            <span className="box-value">
                              ₹{currentSalaryCalc.earnings.hra.amt}
                            </span>
                          </div>

                          <div className="detail-box">
                            <span className="box-label">
                              Standard Allowance (
                              {
                                currentSalaryCalc.earnings
                                  .standardAllowance.pct
                              }
                              %)
                            </span>
                            <span className="box-value">
                              ₹
                              {
                                currentSalaryCalc.earnings
                                  .standardAllowance.amt
                              }
                            </span>
                          </div>

                          <div className="detail-box">
                            <span className="box-label">
                              Performance Bonus (
                              {
                                currentSalaryCalc.earnings
                                  .performanceBonus.pct
                              }
                              %)
                            </span>
                            <span className="box-value">
                              ₹
                              {
                                currentSalaryCalc.earnings
                                  .performanceBonus.amt
                              }
                            </span>
                          </div>

                          <div className="detail-box">
                            <span className="box-label">
                              Leave Travel Allowance (
                              {currentSalaryCalc.earnings.lta.pct}%)
                            </span>
                            <span className="box-value">
                              ₹{currentSalaryCalc.earnings.lta.amt}
                            </span>
                          </div>

                          <div className="detail-box">
                            <span className="box-label">
                              Fixed Allowance (
                              {
                                currentSalaryCalc.earnings
                                  .fixedAllowance.pct
                              }
                              %)
                            </span>
                            <span className="box-value">
                              ₹
                              {
                                currentSalaryCalc.earnings
                                  .fixedAllowance.amt
                              }
                            </span>
                          </div>
                        </div>
                      </div>

                      {/* PF + Tax */}
                      <div
                        style={{
                          display: 'grid',
                          gridTemplateColumns: '1fr 1fr',
                          gap: '2rem'
                        }}
                      >
                        <div>
                          <h4
                            style={{
                              margin: '0 0 1rem 0',
                              color: 'var(--text-main)',
                              fontSize: '1.1rem',
                              borderBottom:
                                '1px solid rgba(0,0,0,0.05)',
                              paddingBottom: '0.5rem'
                            }}
                          >
                            PF Contribution
                          </h4>

                          <div
                            className="admin-detail-grid"
                            style={{
                              gridTemplateColumns: '1fr'
                            }}
                          >
                            <div className="detail-box">
                              <span className="box-label">
                                Employee PF (
                                {
                                  currentSalaryCalc.deductions
                                    .pfEmployee.pct
                                }
                                %)
                              </span>
                              <span className="box-value text-danger">
                                -₹
                                {
                                  currentSalaryCalc.deductions
                                    .pfEmployee.amt
                                }
                              </span>
                            </div>

                            <div className="detail-box">
                              <span className="box-label">
                                Employer PF (
                                {
                                  currentSalaryCalc.deductions
                                    .pfEmployer.pct
                                }
                                %)
                              </span>
                              <span className="box-value">
                                ₹
                                {
                                  currentSalaryCalc.deductions
                                    .pfEmployer.amt
                                }
                              </span>
                            </div>
                          </div>
                        </div>

                        <div>
                          <h4
                            style={{
                              margin: '0 0 1rem 0',
                              color: 'var(--text-main)',
                              fontSize: '1.1rem',
                              borderBottom:
                                '1px solid rgba(0,0,0,0.05)',
                              paddingBottom: '0.5rem'
                            }}
                          >
                            Tax Deductions
                          </h4>

                          <div
                            className="admin-detail-grid"
                            style={{
                              gridTemplateColumns: '1fr'
                            }}
                          >
                            <div className="detail-box">
                              <span className="box-label">
                                Professional Tax (Fixed)
                              </span>
                              <span className="box-value text-danger">
                                -₹
                                {
                                  currentSalaryCalc.deductions
                                    .profTax.amt
                                }
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
            </div>
          </div>
        </main>
      </div>

      {/* Edit Salary Modal */}
      {salaryForm && (
        <Modal
          isOpen={isEditSalaryOpen}
          onClose={() => setIsEditSalaryOpen(false)}
          title={`Edit Salary for ${employee.name}`}
        >
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '1.5rem',
              maxHeight: '65vh',
              overflowY: 'auto',
              paddingRight: '0.5rem'
            }}
          >
            {/* General Salary Fields */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '1rem'
              }}
            >
              <div className="field-group">
                <label className="field-label">
                  Monthly Wage (₹)
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={salaryForm.monthlyWage}
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      monthlyWage: e.target.value
                    })
                  }
                />
              </div>

              <div className="field-group">
                <label className="field-label">
                  Working Days (per week)
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={salaryForm.workingDays}
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      workingDays: e.target.value
                    })
                  }
                />
              </div>

              <div className="field-group">
                <label className="field-label">
                  Break Time
                </label>
                <input
                  type="text"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={salaryForm.breakTime}
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      breakTime: e.target.value
                    })
                  }
                />
              </div>
            </div>

            {/* Component Percentages */}
            <h4
              style={{
                margin: '0.5rem 0 0',
                fontSize: '1rem',
                borderBottom:
                  '1px solid rgba(0,0,0,0.05)',
                paddingBottom: '0.5rem'
              }}
            >
              Component Percentages (%)
            </h4>

            <div
              style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '1rem'
              }}
            >
              <div className="field-group">
                <label className="field-label">
                  Basic Salary %
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={salaryForm.components.basic}
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      components: {
                        ...salaryForm.components,
                        basic: e.target.value
                      }
                    })
                  }
                />
              </div>

              <div className="field-group">
                <label className="field-label">
                  HRA %
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={salaryForm.components.hra}
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      components: {
                        ...salaryForm.components,
                        hra: e.target.value
                      }
                    })
                  }
                />
              </div>

              <div className="field-group">
                <label className="field-label">
                  Standard Allowance %
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={
                    salaryForm.components.standardAllowance
                  }
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      components: {
                        ...salaryForm.components,
                        standardAllowance: e.target.value
                      }
                    })
                  }
                />
              </div>

              <div className="field-group">
                <label className="field-label">
                  Performance Bonus %
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={
                    salaryForm.components.performanceBonus
                  }
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      components: {
                        ...salaryForm.components,
                        performanceBonus: e.target.value
                      }
                    })
                  }
                />
              </div>

              <div className="field-group">
                <label className="field-label">
                  LTA %
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={salaryForm.components.lta}
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      components: {
                        ...salaryForm.components,
                        lta: e.target.value
                      }
                    })
                  }
                />
              </div>

              <div className="field-group">
                <label className="field-label">
                  Fixed Allowance %
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={
                    salaryForm.components.fixedAllowance
                  }
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      components: {
                        ...salaryForm.components,
                        fixedAllowance: e.target.value
                      }
                    })
                  }
                />
              </div>
            </div>

            {/* PF and Tax */}
            <h4
              style={{
                margin: '0.5rem 0 0',
                fontSize: '1rem',
                borderBottom:
                  '1px solid rgba(0,0,0,0.05)',
                paddingBottom: '0.5rem'
              }}
            >
              PF & Taxes
            </h4>

            <div
              style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '1rem'
              }}
            >
              <div className="field-group">
                <label className="field-label">
                  Employee PF %
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={salaryForm.pf.employee}
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      pf: {
                        ...salaryForm.pf,
                        employee: e.target.value
                      }
                    })
                  }
                />
              </div>

              <div className="field-group">
                <label className="field-label">
                  Employer PF %
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={salaryForm.pf.employer}
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      pf: {
                        ...salaryForm.pf,
                        employer: e.target.value
                      }
                    })
                  }
                />
              </div>

              <div
                className="field-group"
                style={{ gridColumn: '1 / -1' }}
              >
                <label className="field-label">
                  Professional Tax (Fixed ₹)
                </label>
                <input
                  type="number"
                  className="input-field"
                  style={{ padding: '0 1rem' }}
                  value={salaryForm.tax.professional}
                  onChange={(e) =>
                    setSalaryForm({
                      ...salaryForm,
                      tax: {
                        ...salaryForm.tax,
                        professional: e.target.value
                      }
                    })
                  }
                />
              </div>
            </div>
          </div>

          <Button
            onClick={handleSaveSalary}
            className="mt-4"
            style={{ width: '100%' }}
          >
            Save Salary Details
          </Button>
        </Modal>
      )}
    </>
  );
};