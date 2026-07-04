import { useState } from 'react';
import { Users, Plane, LogOut, Search, Plus, X } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

export const DashboardPage = () => {
  const [isCheckedIn, setIsCheckedIn] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const { logout } = useAuth();

  const handleCheckIn = () => {
    setIsCheckedIn(!isCheckedIn);
  };

  const employees = [
    { id: 1, name: 'Sarah Jenkins', empId: 'EMP-001', email: 'sarah.j@corehr.com', status: 'present' },
    { id: 2, name: 'Michael Chen', empId: 'EMP-002', email: 'michael.c@corehr.com', status: 'leave' },
    { id: 3, name: 'Emily Davis', empId: 'EMP-003', email: 'emily.d@corehr.com', status: 'absent' },
    { id: 4, name: 'Robert Fox', empId: 'EMP-004', email: 'robert.f@corehr.com', status: 'present' },
    { id: 5, name: 'Cody Fisher', empId: 'EMP-005', email: 'cody.f@corehr.com', status: 'present' },
    { id: 6, name: 'Esther Howard', empId: 'EMP-006', email: 'esther.h@corehr.com', status: 'leave' },
    { id: 7, name: 'Jenny Wilson', empId: 'EMP-007', email: 'jenny.w@corehr.com', status: 'present' },
    { id: 8, name: 'Guy Hawkins', empId: 'EMP-008', email: 'guy.h@corehr.com', status: 'absent' },
    { id: 9, name: 'Jacob Jones', empId: 'EMP-009', email: 'jacob.j@corehr.com', status: 'present' },
  ];

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
          <button className="nav-link">Attendance</button>
          <button className="nav-link">Time Off</button>
        </nav>

        <div className="dashboard-actions">
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
        
        {/* ── Subheader / Toolbar ── */}
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
          {[...employees]
            .sort((a, b) => {
              if (!searchQuery) return 0;
              const aMatch = a.name.toLowerCase().includes(searchQuery.toLowerCase());
              const bMatch = b.name.toLowerCase().includes(searchQuery.toLowerCase());
              if (aMatch && !bMatch) return -1;
              if (!aMatch && bMatch) return 1;
              return 0;
            })
            .map((emp) => {
            const isHighlighted = searchQuery && emp.name.toLowerCase().includes(searchQuery.toLowerCase());
            
            return (
              <div 
                key={emp.id} 
                className={`employee-card-minimal ${isHighlighted ? 'highlighted' : ''}`}
                onClick={() => setSelectedEmployee(emp)}
                style={{ cursor: 'pointer' }}
              >
                <div className="employee-avatar-large">
                  <Users size={40} />
                </div>
                <div className="employee-name-minimal">{emp.name}</div>
                
                {/* Keep status indicators small and absolutely positioned, or directly below name */}
                <div className={`employee-status-minimal status-${emp.status}`}>
                  {emp.status === 'present' && <div className="status-dot"></div>}
                  {emp.status === 'leave' && <Plane size={14} />}
                  {emp.status === 'absent' && <div className="status-dot"></div>}
                </div>
              </div>
            );
          })}
        </div>
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
                <h2>{selectedEmployee.name}</h2>
                <div className={`preview-status status-${selectedEmployee.status}`}>
                  {selectedEmployee.status === 'present' && <><div className="status-dot"></div> Present</>}
                  {selectedEmployee.status === 'leave' && <><Plane size={16} /> On Leave</>}
                  {selectedEmployee.status === 'absent' && <><div className="status-dot"></div> Absent</>}
                </div>
              </div>
            </div>

            <div className="preview-details">
              <div className="detail-row">
                <span className="detail-label">Employee ID</span>
                <span className="detail-value emp-id">{selectedEmployee.empId}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Email Address</span>
                <span className="detail-value">{selectedEmployee.email}</span>
              </div>
              {/* Could add more fields here if desired */}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
