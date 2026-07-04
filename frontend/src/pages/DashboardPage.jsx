import { useState, useRef, useEffect } from 'react';
import { Users, Plane, LogOut, Search, Plus, X, User, FileText, DollarSign, Calendar, Shield, MapPin, Phone, Briefcase, Mail, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { mockEmployees } from '../data/mockEmployees';

export const DashboardPage = () => {
  const [isCheckedIn, setIsCheckedIn] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [drawerTab, setDrawerTab] = useState('Overview');
  const { user, logout, isCheckedInToday, checkIn, checkOut } = useAuth();
  const [deleteTrigger, setDeleteTrigger] = useState(0);
  const profileMenuRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (profileMenuRef.current && !profileMenuRef.current.contains(event.target)) {
        setIsProfileMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    setIsCheckedIn(isCheckedInToday ? isCheckedInToday() : false);
  }, [isCheckedInToday]);

  const handleCheckIn = () => {
    if (isCheckedIn) {
      if (checkOut) checkOut();
      setIsCheckedIn(false);
    } else {
      if (checkIn) checkIn();
      setIsCheckedIn(true);
    }
  };

  const deleteEmployee = (employeeId) => {
    if (!window.confirm("Are you sure you want to delete this employee?")) return;
    
    const strId = String(employeeId);
    
    // Add to soft-delete array to hide hardcoded mock employees
    const deleted = JSON.parse(localStorage.getItem('mock_db_deleted') || '[]');
    if (!deleted.includes(strId)) {
      deleted.push(strId);
      localStorage.setItem('mock_db_deleted', JSON.stringify(deleted));
    }
    
    // Hard-delete from locally created users database
    const localUsers = JSON.parse(localStorage.getItem('mock_db_users') || '[]');
    const updatedLocalUsers = localUsers.filter(u => String(u.id) !== strId && String(u.empId) !== strId);
    localStorage.setItem('mock_db_users', JSON.stringify(updatedLocalUsers));
    
    setDeleteTrigger(prev => prev + 1);
  };

  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };



  return (
    <div className="dashboard-container">
      {/* ── Header ── */}
      <header className="dashboard-header">
        <div className="dashboard-brand">
          <div className="dashboard-logo">
            <Users size={20} />
          </div>
          <h1 className="dashboard-title">CoreHR</h1>
        </div>

        <nav className="dashboard-nav">
          <button className="nav-link active">Employees</button>
          <button className="nav-link" onClick={() => navigate('/attendance')}>Attendance</button>
          <button className="nav-link" onClick={() => navigate('/attendance')}>Time Off</button>
        </nav>

        <div className="dashboard-actions">
          {/* Check-in Widget */}
          <div className="checkin-widget" onClick={handleCheckIn} title="Click to Check In/Out">
            <div className={`checkin-circle ${isCheckedIn ? 'checked-in' : 'checked-out'}`}></div>
            <span className="checkin-text">
              {isCheckedIn ? 'Checked In' : 'Checked Out'}
            </span>
          </div>
          <div className="profile-menu-container" ref={profileMenuRef}>
            <div
              className="profile-trigger"
              onClick={() => setIsProfileMenuOpen(!isProfileMenuOpen)}
            >
              <div className="profile-avatar" style={{ overflow: 'hidden' }}>
                {user?.avatarUrl ? (
                  <img src={user.avatarUrl} alt={user.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} onError={(e) => e.target.style.display = 'none'} />
                ) : (
                  getInitials(user?.name)
                )}
              </div>
              <div className="profile-info">
                <span className="profile-name">{user?.name || 'User'}</span>
                <span className="profile-role">{user?.role || 'Employee'}</span>
              </div>
            </div>

            {isProfileMenuOpen && (
              <div className="profile-dropdown">
                <div className="dropdown-header">
                  <strong>{user?.name}</strong>
                </div>
                <div className="dropdown-divider"></div>
                <button className="dropdown-item" onClick={() => navigate(`/profile?tab=Overview`)}>
                  <User size={16} /> My Profile
                </button>
                <button className="dropdown-item" onClick={() => navigate(`/profile?tab=Salary Info`)}>
                  <DollarSign size={16} /> Salary Slip
                </button>
                <button className="dropdown-item" onClick={() => navigate(`/profile?tab=Attendance`)}>
                  <Calendar size={16} /> My Attendance
                </button>
                <button className="dropdown-item" onClick={() => navigate(`/profile?tab=Resume`)}>
                  <FileText size={16} /> Resume
                </button>
                <div className="dropdown-divider"></div>
                <button className="dropdown-item text-danger" onClick={logout}>
                  <LogOut size={16} /> Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* ── Main Content ── */}
      <main className="dashboard-main">

        {/* ── Page Banner ── */}
        <div className="page-banner">
          <h2>Employee Directory</h2>
          <p>Manage your team members, view their status, and add new employees to the organization.</p>
        </div>

        {/* ── Subheader / Toolbar ── */}
        <div className="dashboard-toolbar">
          <h2 className="section-title" style={{ marginBottom: 0, fontSize: '1.25rem' }}>All Employees</h2>

          <div className="toolbar-actions">
            <div className="search-box">
              <Search size={18} className="search-icon" />
              <input
                type="text"
                placeholder="Search by full name or ID..."
                className="search-input"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            {user?.role === 'Admin' && (
              <button className="btn btn-primary add-user-btn" onClick={() => navigate('/register')}>
                <Plus size={18} />
                Add New User
              </button>
            )}
          </div>
        </div>

        <div className="employee-grid">
          {(() => {
            const localUsers = JSON.parse(localStorage.getItem('mock_db_users')) || [];
            const deleted = JSON.parse(localStorage.getItem('mock_db_deleted') || '[]');
            const combined = [...mockEmployees];
            localUsers.forEach(u => {
              if (!combined.find(m => m.email === u.email)) {
                combined.push(u);
              }
            });
            return combined.filter(u => !deleted.includes(String(u.id)) && !deleted.includes(String(u.empId)));
          })()
            .sort((a, b) => {
              if (!searchQuery) return 0;
              const q = searchQuery.toLowerCase();
              
              const aName = a.name || a.fullName || '';
              const aId = (a.empId || a.id || '').toString();
              const aMatch = aName.toLowerCase().includes(q) || aId.toLowerCase().includes(q);
              
              const bName = b.name || b.fullName || '';
              const bId = (b.empId || b.id || '').toString();
              const bMatch = bName.toLowerCase().includes(q) || bId.toLowerCase().includes(q);
              
              if (aMatch && !bMatch) return -1;
              if (!aMatch && bMatch) return 1;
              return 0;
            })
            .map((emp) => {
              const q = searchQuery ? searchQuery.toLowerCase() : '';
              const empName = emp.name || emp.fullName || '';
              const empId = (emp.empId || emp.id || '').toString();
              const isHighlighted = q && (empName.toLowerCase().includes(q) || empId.toLowerCase().includes(q));
              
              return (
                <div 
                  key={emp.id} 
                  className={`employee-card-compact ${isHighlighted ? 'highlighted' : ''}`}
                  style={{ position: 'relative' }}
                  onClick={() => {
                    setSelectedEmployee(emp);
                    setDrawerTab('Overview');
                  }}
                >    
                  {user?.role === 'Admin' && (
                    <button
                      className="delete-employee-btn"
                      onClick={(e) => { 
                        e.stopPropagation(); 
                        deleteEmployee(emp.id); 
                      }}
                      style={{
                        position: 'absolute',
                        top: '8px',
                        right: '8px',
                        background: 'transparent',
                        border: 'none',
                        color: 'var(--error)',
                        cursor: 'pointer',
                        padding: '6px',
                        zIndex: 15,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                      title="Delete Employee"
                    >
                      <Trash2 size={16} />
                    </button>
                  )}
                  <div className="emp-card-avatar" style={{ overflow: 'hidden' }}>
                    {emp.avatarUrl ? (
                      <img
                        src={emp.avatarUrl}
                        alt={empName}
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                        onError={(e) => {
                          e.target.style.display = 'none';
                          e.target.nextSibling.style.display = 'flex';
                        }}
                      />
                    ) : null}
                    <div style={{ display: emp.avatarUrl ? 'none' : 'flex', width: '100%', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
                      {getInitials(empName || 'User')}
                    </div>
                  </div>
                  <div className="emp-card-info">
                    <div className="emp-card-name">{empName || 'User'}</div>
                    <div className="emp-card-role">{emp.role}</div>
                    <div className="emp-card-meta">
                      <span className="emp-card-id">{emp.empId}</span> • <span>{emp.department}</span>
                    </div>
                  </div>

                  <div className={`badge badge-${emp.status}`}>
                    {emp.status}
                  </div>

                  {/* Hover Overlay */}
                  <div className="emp-card-overlay">
                    <div className="overlay-info" style={{ textAlign: 'left', marginBottom: '0.5rem' }}>
                      <div style={{ fontSize: '0.875rem', fontWeight: '500', color: 'var(--text-main)', marginBottom: '0.25rem' }}><Mail size={12} style={{ display: 'inline', marginRight: '4px' }} /> {emp.email}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}><Phone size={12} style={{ display: 'inline', marginRight: '4px' }} /> {emp.phone}</div>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button
                        className="btn btn-sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedEmployee(emp);
                          setDrawerTab('Overview');
                        }}
                        style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem', background: 'var(--bg-input)' }}
                      >
                        Quick Preview
                      </button>
                      <button
                        className="btn btn-primary btn-sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/employees/${emp.id}`);
                        }}
                        style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem' }}
                      >
                        Full Profile
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
        </div>
      </main>

      {/* ── Employee Preview Drawer ── */}
      {selectedEmployee && (
        <div className="drawer-overlay" onClick={() => setSelectedEmployee(null)}>
          <div className="drawer-content" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div className="drawer-profile">
                <div className="drawer-avatar" style={{ overflow: 'hidden' }}>
                  {selectedEmployee.avatarUrl ? (
                    <img
                      src={selectedEmployee.avatarUrl}
                      alt={selectedEmployee.name}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                      onError={(e) => {
                        e.target.style.display = 'none';
                        e.target.nextSibling.style.display = 'flex';
                      }}
                    />
                  ) : null}
                  <div style={{ display: selectedEmployee.avatarUrl ? 'none' : 'flex', width: '100%', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
                    {getInitials(selectedEmployee.name)}
                  </div>
                </div>
                <div>
                  <h2 style={{ fontSize: '1.25rem', margin: '0 0 0.25rem 0' }}>{selectedEmployee.name}</h2>
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>{selectedEmployee.role}</div>
                </div>
              </div>
              <button className="drawer-close" onClick={() => setSelectedEmployee(null)}>
                <X size={20} />
              </button>
            </div>

            <div className="drawer-body">
              <div className="preview-details">
                <div className="detail-row"><span className="detail-label">Status</span><span className={`badge badge-${selectedEmployee.status}`} style={{ alignSelf: 'flex-start' }}>{selectedEmployee.status}</span></div>
                <div className="detail-row"><span className="detail-label">Employee ID</span><span className="detail-value">{selectedEmployee.empId}</span></div>
                <div className="detail-row"><span className="detail-label">Email</span><span className="detail-value"><a href={`mailto:${selectedEmployee.email}`}>{selectedEmployee.email}</a></span></div>
                <div className="detail-row"><span className="detail-label">Phone</span><span className="detail-value">{selectedEmployee.phone}</span></div>
                <div className="detail-row"><span className="detail-label">Department</span><span className="detail-value">{selectedEmployee.department}</span></div>
                <div className="detail-row"><span className="detail-label">Manager</span><span className="detail-value">{selectedEmployee.manager}</span></div>
                <div className="detail-row"><span className="detail-label">Location</span><span className="detail-value">{selectedEmployee.location}</span></div>
                <div className="detail-row"><span className="detail-label">Joining Date</span><span className="detail-value">{selectedEmployee.joiningDate}</span></div>
              </div>
            </div>

            <div className="drawer-footer">
              <button
                className="btn btn-primary"
                style={{ flex: 1 }}
                onClick={() => navigate(`/employees/${selectedEmployee.id}`)}
              >
                View Full Profile
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
