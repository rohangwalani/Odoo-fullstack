import { useState, useEffect } from 'react';
import { Users, Plane, LogOut, Search, Plus, X, Calendar, CheckCircle } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import axiosInstance from '../api/axiosInstance';
import toast from 'react-hot-toast';

export const DashboardPage = () => {
  const [isCheckedIn, setIsCheckedIn] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const { user, logout } = useAuth();
  const [employees, setEmployees] = useState([]);
  const [stats, setStats] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        if (user?.role === 'ADMIN') {
          const statsRes = await axiosInstance.get('/dashboard/admin');
          setStats(statsRes.data);
          const empRes = await axiosInstance.get('/employees');
          setEmployees(empRes.data);
        } else if (user?.role === 'EMPLOYEE') {
          const statsRes = await axiosInstance.get('/dashboard/employee');
          setStats(statsRes.data);
        }
      } catch (error) {
        console.error("Failed to fetch dashboard data");
      }
    };
    if (user) fetchData();
  }, [user]);

  const handleCheckIn = async () => {
    try {
      if (isCheckedIn) {
        await axiosInstance.post('/attendance/check-out', {});
        toast.success("Checked out successfully");
      } else {
        await axiosInstance.post('/attendance/check-in', {});
        toast.success("Checked in successfully");
      }
      setIsCheckedIn(!isCheckedIn);
    } catch (error) {
      // error handled by interceptor
    }
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
          <button className="nav-link active">Dashboard</button>
        </nav>

        <div className="dashboard-actions">
          <div className="user-welcome" style={{ marginRight: '1rem', fontWeight: 500, color: 'var(--text-secondary)' }}>
            Welcome, {user?.name}
          </div>
          {/* Check-in Widget */}
          <div className="checkin-widget" onClick={handleCheckIn} title="Click to Check In/Out">
            <div className={`checkin-circle ${isCheckedIn ? 'checked-in' : 'checked-out'}`}></div>
            <span className="checkin-text">
              {isCheckedIn ? 'Checked In' : 'Checked Out'}
            </span>
          </div>
          
          <button onClick={logout} className="nav-link" title="Logout">
            <LogOut size={20} />
          </button>
        </div>
      </header>

      {/* ── Main Content ── */}
      <main className="dashboard-main">
        {user?.role === 'ADMIN' ? (
          <>
            <div className="dashboard-toolbar">
              <h2 className="section-title" style={{ marginBottom: 0 }}>Employee Directory</h2>
              <div className="toolbar-actions">
                <div className="search-box">
                  <Search size={18} className="search-icon" />
                  <input 
                    type="text" 
                    placeholder="Search employee..." 
                    className="search-input"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </div>
                <button className="btn btn-primary add-user-btn">
                  <Plus size={18} />
                  Add New User
                </button>
              </div>
            </div>
            
            <div className="employee-grid">
              {employees
                .filter(emp => {
                  if (!searchQuery) return true;
                  const name = `${emp.firstName} ${emp.lastName}`.toLowerCase();
                  return name.includes(searchQuery.toLowerCase());
                })
                .map((emp) => (
                  <div 
                    key={emp.id} 
                    className="employee-card-minimal"
                    onClick={() => setSelectedEmployee(emp)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="employee-avatar-large">
                      <Users size={40} />
                    </div>
                    <div className="employee-name-minimal">{emp.firstName} {emp.lastName}</div>
                  </div>
              ))}
            </div>
          </>
        ) : (
          <>
            <h2 className="section-title">My Dashboard</h2>
            <div className="employee-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
              <div className="employee-card-minimal">
                <CheckCircle size={32} color="var(--success)" />
                <div style={{ marginTop: '1rem', fontSize: '1.2rem', fontWeight: 'bold' }}>{stats?.presentDays || 0}</div>
                <div style={{ color: 'var(--text-secondary)' }}>Present Days</div>
              </div>
              <div className="employee-card-minimal">
                <X size={32} color="var(--error)" />
                <div style={{ marginTop: '1rem', fontSize: '1.2rem', fontWeight: 'bold' }}>{stats?.absentDays || 0}</div>
                <div style={{ color: 'var(--text-secondary)' }}>Absent Days</div>
              </div>
              <div className="employee-card-minimal">
                <Calendar size={32} color="var(--warning)" />
                <div style={{ marginTop: '1rem', fontSize: '1.2rem', fontWeight: 'bold' }}>{stats?.pendingLeaves || 0}</div>
                <div style={{ color: 'var(--text-secondary)' }}>Pending Leaves</div>
              </div>
              <div className="employee-card-minimal">
                <Plane size={32} color="var(--primary)" />
                <div style={{ marginTop: '1rem', fontSize: '1.2rem', fontWeight: 'bold' }}>{stats?.approvedLeaves || 0}</div>
                <div style={{ color: 'var(--text-secondary)' }}>Approved Leaves</div>
              </div>
            </div>
          </>
        )}
      </main>

      {/* ── Employee Preview Modal ── */}
      {selectedEmployee && (
        <div className="preview-modal-overlay" onClick={() => setSelectedEmployee(null)}>
          <div className="preview-modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="preview-close-btn" onClick={() => setSelectedEmployee(null)}>
              <X size={24} />
            </button>
            
            <div className="preview-header">
              <div className="preview-avatar">
                <Users size={64} />
              </div>
              <div className="preview-title-box">
                <h2>{selectedEmployee.firstName} {selectedEmployee.lastName}</h2>
              </div>
            </div>

            <div className="preview-details">
              <div className="detail-row">
                <span className="detail-label">Employee ID</span>
                <span className="detail-value emp-id">{selectedEmployee.loginId}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Email Address</span>
                <span className="detail-value">{selectedEmployee.email}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Department</span>
                <span className="detail-value">{selectedEmployee.department || 'N/A'}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Role</span>
                <span className="detail-value">{selectedEmployee.role}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
