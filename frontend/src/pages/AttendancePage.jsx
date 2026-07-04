import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { mockEmployees } from '../data/mockEmployees';
import { Users, Calendar, ArrowLeft, Clock, Search, LogOut, CheckCircle, AlertCircle, Calendar as CalendarIcon, ChevronLeft, ChevronRight, Plus, FileText, Check, X, Edit2 } from 'lucide-react';
import { Modal } from '../components/Modal';

export const AttendancePage = () => {
  const { user, logout, getAllAttendance, getAttendanceForUser, getAllLeaves, getLeavesForUser, requestLeave, updateLeaveStatus } = useAuth();
  const navigate = useNavigate();
  const [mainTab, setMainTab] = useState('Attendance Log'); // 'Attendance Log' or 'Time Off'
  const [viewMode, setViewMode] = useState('Day'); // 'Day' or 'Month' for Admin
  const [currentDate, setCurrentDate] = useState(new Date());
  const [searchQuery, setSearchQuery] = useState('');
  const [allUsers, setAllUsers] = useState([]);
  const [attendanceData, setAttendanceData] = useState({});
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [isLeaveModalOpen, setIsLeaveModalOpen] = useState(false);
  const [leaveForm, setLeaveForm] = useState({
    type: 'Paid Time Off',
    fromDate: '',
    toDate: '',
    reason: ''
  });
  const [isEditAttOpen, setIsEditAttOpen] = useState(false);
  const [attForm, setAttForm] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const profileMenuRef = useRef(null);

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
    // Generate dummy data if empty
    const att = getAllAttendance();
    const storedUsers = JSON.parse(localStorage.getItem('mock_db_users') || '[]');
    const combined = [...mockEmployees];
    storedUsers.forEach(u => {
      if (!combined.find(m => m.email === u.email)) {
        combined.push(u);
      }
    });
    setAllUsers(combined);
    
    // Seed past 30 days of dummy attendance for any user that has NO data
    let dataChanged = false;
    const dummyAtt = { ...att };
    
    combined.forEach(emp => {
      if (!dummyAtt[emp.id]) dummyAtt[emp.id] = {};
      
      // If this user has incomplete records (e.g. missing weekends), regenerate them completely
      if (Object.keys(dummyAtt[emp.id]).length < 28) {
        dummyAtt[emp.id] = {};
        dataChanged = true;
        for (let i = 0; i < 30; i++) {
          const d = new Date();
          d.setDate(d.getDate() - i);
          
          const dateStr = d.toISOString().split('T')[0];
          
          // Randomly make them absent for a day (~15% chance of absence to be realistic)
          if (Math.random() > 0.85) continue;

          const checkIn = new Date(d);
          checkIn.setHours(8, Math.floor(Math.random() * 45) + 15, 0); // 8:15 to 9:00
          
          const checkOut = new Date(d);
          checkOut.setHours(17, Math.floor(Math.random() * 60), 0); // 17:00 to 18:00
          
          dummyAtt[emp.id][dateStr] = {
            date: dateStr,
            checkInTime: checkIn.toISOString(),
            checkOutTime: checkOut.toISOString(),
          };
        }
      }
    });

    if (dataChanged) {
      localStorage.setItem('mock_db_attendance', JSON.stringify(dummyAtt));
    }
    setAttendanceData(dummyAtt);
  }, []);

  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  const calculateHours = (checkInStr, checkOutStr, breakTimeStr = '1h') => {
    if (!checkInStr || !checkOutStr) return { work: 0, extra: 0 };
    const inTime = new Date(checkInStr);
    const outTime = new Date(checkOutStr);
    let diffHours = (outTime - inTime) / (1000 * 60 * 60);
    
    // deduct break time roughly
    if (breakTimeStr.includes('1h')) diffHours -= 1;
    else if (breakTimeStr.includes('30m')) diffHours -= 0.5;

    if (diffHours < 0) diffHours = 0;
    
    let extra = 0;
    if (diffHours > 8) {
      extra = diffHours - 8;
      diffHours = 8;
    }
    
    return { work: diffHours, extra };
  };

  const formatDateLabel = (date) => {
    return date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
  };
  
  const formatMonthLabel = (date) => {
    return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  };

  const handlePrev = () => {
    const newDate = new Date(currentDate);
    if (viewMode === 'Day') newDate.setDate(newDate.getDate() - 1);
    else newDate.setMonth(newDate.getMonth() - 1);
    setCurrentDate(newDate);
  };

  const handleNext = () => {
    const newDate = new Date(currentDate);
    if (viewMode === 'Day') newDate.setDate(newDate.getDate() + 1);
    else newDate.setMonth(newDate.getMonth() + 1);
    setCurrentDate(newDate);
  };

  const formatTime = (isoString) => {
    if (!isoString) return '--:--';
    return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  // --- ADMIN VIEW ---
  const renderAdminView = () => {
    const dateStr = currentDate.toISOString().split('T')[0];
    
    const filteredUsers = allUsers.filter(u => {
      if (!searchQuery) return true;
      const q = searchQuery.toLowerCase();
      const n = (u.name || u.fullName || '').toLowerCase();
      return n.includes(q);
    });

    return (
      <div className="tab-pane">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
          <h2 className="section-title" style={{ margin: 0 }}>Attendance Log</h2>
          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
            <div className="search-box" style={{ maxWidth: '250px' }}>
              <Search size={18} className="search-icon" />
              <input
                type="text"
                placeholder="Search employee..."
                className="search-input"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="segmented-control" style={{ display: 'flex', background: 'var(--bg-card)', border: '1px solid rgba(0,0,0,0.1)', borderRadius: '6px', overflow: 'hidden' }}>
              <button className={`btn-segment ${viewMode === 'Day' ? 'active' : ''}`} onClick={() => setViewMode('Day')} style={{ padding: '0.4rem 1rem', border: 'none', background: viewMode === 'Day' ? 'var(--accent-primary)' : 'transparent', color: viewMode === 'Day' ? 'white' : 'var(--text-main)', cursor: 'pointer' }}>Day</button>
              <button className={`btn-segment ${viewMode === 'Month' ? 'active' : ''}`} onClick={() => setViewMode('Month')} style={{ padding: '0.4rem 1rem', border: 'none', background: viewMode === 'Month' ? 'var(--accent-primary)' : 'transparent', color: viewMode === 'Month' ? 'white' : 'var(--text-main)', cursor: 'pointer' }}>Month</button>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem', background: 'var(--bg-card)', padding: '0.5rem 1rem', borderRadius: '8px', width: 'fit-content', border: '1px solid rgba(0,0,0,0.05)' }}>
          <button onClick={handlePrev} className="btn-icon" style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--text-muted)' }}><ChevronLeft size={20} /></button>
          <span style={{ fontWeight: 600, minWidth: '150px', textAlign: 'center' }}>
            {viewMode === 'Day' ? formatDateLabel(currentDate) : formatMonthLabel(currentDate)}
          </span>
          <button onClick={handleNext} className="btn-icon" style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--text-muted)' }}><ChevronRight size={20} /></button>
        </div>

        <div className="table-responsive" style={{ background: 'var(--bg-card)', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead style={{ background: 'rgba(0,0,0,0.02)' }}>
              <tr>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Employee</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Status</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Check In</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Check Out</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Work Hrs</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Extra Hrs</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600, width: '50px' }}></th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map(emp => {
                const record = attendanceData[emp.id]?.[dateStr];
                const { work, extra } = calculateHours(record?.checkInTime, record?.checkOutTime, emp.salaryInfo?.breakTime);
                const isPresent = !!record;
                const isWeekend = new Date(dateStr).getDay() === 0 || new Date(dateStr).getDay() === 6;
                
                if (viewMode === 'Month') {
                  // For month view, just show summary in table instead of individual days for all users
                  const monthPrefix = currentDate.toISOString().split('-').slice(0,2).join('-');
                  let daysPresent = 0;
                  let totalWorkHours = 0;
                  if (attendanceData[emp.id]) {
                    Object.keys(attendanceData[emp.id]).forEach(k => {
                      if (k.startsWith(monthPrefix)) {
                        daysPresent++;
                        const dRecord = attendanceData[emp.id][k];
                        const hrs = calculateHours(dRecord.checkInTime, dRecord.checkOutTime, emp.salaryInfo?.breakTime);
                        totalWorkHours += hrs.work + hrs.extra;
                      }
                    });
                  }
                  return (
                    <tr key={emp.id} style={{ borderBottom: '1px solid rgba(0,0,0,0.05)' }}>
                      <td style={{ padding: '1rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                          <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: 'var(--accent-light)', color: 'var(--accent-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600, fontSize: '0.8rem' }}>
                            {getInitials(emp.name || emp.fullName)}
                          </div>
                          <span style={{ fontWeight: 500 }}>{emp.name || emp.fullName}</span>
                        </div>
                      </td>
                      <td colSpan="6" style={{ padding: '1rem', color: 'var(--text-muted)' }}>
                        <span style={{ fontWeight: 500, color: 'var(--text-main)' }}>{daysPresent} Days Present</span> in {formatMonthLabel(currentDate)} • Total Hours: {totalWorkHours.toFixed(1)}h
                      </td>
                    </tr>
                  );
                }

                return (
                  <tr key={emp.id} style={{ borderBottom: '1px solid rgba(0,0,0,0.05)' }}>
                    <td style={{ padding: '1rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: 'var(--accent-light)', color: 'var(--accent-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600, fontSize: '0.8rem', overflow: 'hidden' }}>
                          {emp.avatarUrl ? <img src={emp.avatarUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} onError={(e) => e.target.style.display='none'}/> : getInitials(emp.name || emp.fullName)}
                        </div>
                        <div>
                          <div style={{ fontWeight: 500 }}>{emp.name || emp.fullName}</div>
                          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{emp.department}</div>
                        </div>
                      </div>
                    </td>
                    <td style={{ padding: '1rem' }}>
                      {isPresent ? (
                         <span className="badge badge-present" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}><CheckCircle size={12}/> Present</span>
                      ) : (
                         <span className="badge badge-absent" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}><AlertCircle size={12}/> Absent</span>
                      )}
                    </td>
                    <td style={{ padding: '1rem' }}>{formatTime(record?.checkInTime)}</td>
                    <td style={{ padding: '1rem' }}>{formatTime(record?.checkOutTime)}</td>
                    <td style={{ padding: '1rem' }}>{work > 0 ? `${work.toFixed(1)}h` : '-'}</td>
                    <td style={{ padding: '1rem' }}>{extra > 0 ? <span style={{ color: 'var(--accent-primary)', fontWeight: 500 }}>{extra.toFixed(1)}h</span> : '-'}</td>
                    <td style={{ padding: '1rem' }}>
                      <button 
                        className="btn btn-sm" 
                        onClick={() => {
                          setAttForm({
                            empId: emp.id,
                            empName: emp.name || emp.fullName,
                            dateStr: dateStr,
                            checkInTime: record?.checkInTime ? new Date(record.checkInTime).toISOString().substring(11,16) : '',
                            checkOutTime: record?.checkOutTime ? new Date(record.checkOutTime).toISOString().substring(11,16) : ''
                          });
                          setIsEditAttOpen(true);
                        }}
                        style={{ padding: '0.4rem', border: '1px solid #e5e7eb', background: '#fff', borderRadius: '4px', cursor: 'pointer', color: 'var(--text-muted)' }}
                        title="Edit Attendance"
                      >
                        <Edit2 size={14} />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  // --- EMPLOYEE VIEW ---
  const renderEmployeeView = () => {
    const monthPrefix = currentDate.toISOString().split('-').slice(0,2).join('-');
    const myAtt = attendanceData[user?.id] || {};
    
    const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate();
    let daysPresent = 0;
    let totalWork = 0;
    
    // Generate full month days
    const monthDays = [];
    for (let i = 1; i <= daysInMonth; i++) {
      const d = new Date(currentDate.getFullYear(), currentDate.getMonth(), i);
      const ds = d.toISOString().split('T')[0];
      const isWeekend = d.getDay() === 0 || d.getDay() === 6;
      monthDays.push({ date: d, dateStr: ds, isWeekend });
    }

    monthDays.forEach(day => {
      const record = myAtt[day.dateStr];
      if (record) {
        daysPresent++;
        const { work, extra } = calculateHours(record.checkInTime, record.checkOutTime, user?.salaryInfo?.breakTime);
        totalWork += work + extra;
      }
    });
    
    let workingDaysCount = monthDays.filter(d => !d.isWeekend).length;
    let leavesCount = workingDaysCount - daysPresent;
    if (leavesCount < 0) leavesCount = 0;

    return (
      <div className="tab-pane">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <h2 className="section-title" style={{ margin: 0 }}>My Attendance</h2>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', background: 'var(--bg-card)', padding: '0.5rem 1rem', borderRadius: '8px', border: '1px solid rgba(0,0,0,0.05)' }}>
            <button onClick={handlePrev} className="btn-icon" style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--text-muted)' }}><ChevronLeft size={20} /></button>
            <span style={{ fontWeight: 600, minWidth: '150px', textAlign: 'center' }}>
              {formatMonthLabel(currentDate)}
            </span>
            <button onClick={handleNext} className="btn-icon" style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--text-muted)' }}><ChevronRight size={20} /></button>
          </div>
        </div>

        {/* Summary Cards */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
          <div style={{ background: 'var(--bg-card)', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: 500 }}>Total Working Days</span>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--text-main)' }}>{workingDaysCount}</div>
          </div>
          <div style={{ background: 'var(--bg-card)', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', gap: '0.5rem', borderLeft: '4px solid var(--accent-primary)' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: 500 }}>Days Present</span>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--accent-primary)' }}>{daysPresent}</div>
          </div>
          <div style={{ background: 'var(--bg-card)', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', gap: '0.5rem', borderLeft: '4px solid #ef4444' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: 500 }}>Absences/Leaves</span>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#ef4444' }}>{leavesCount}</div>
          </div>
          <div style={{ background: 'var(--bg-card)', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: 500 }}>Total Hours Logged</span>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--text-main)' }}>{totalWork.toFixed(1)} <span style={{ fontSize: '1rem', fontWeight: 400, color: 'var(--text-muted)' }}>hrs</span></div>
          </div>
        </div>

        <div className="table-responsive" style={{ background: 'var(--bg-card)', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead style={{ background: 'rgba(0,0,0,0.02)' }}>
              <tr>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Date</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Status</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Check In</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Check Out</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Work Hrs</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Extra Hrs</th>
              </tr>
            </thead>
            <tbody>
              {monthDays.map(day => {
                const record = myAtt[day.dateStr];
                const { work, extra } = calculateHours(record?.checkInTime, record?.checkOutTime, user?.salaryInfo?.breakTime);
                const isPresent = !!record;

                let statusBadge;
                if (isPresent) {
                  statusBadge = <span className="badge badge-present" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}><CheckCircle size={12}/> Present</span>;
                } else if (day.date < new Date(new Date().setHours(0,0,0,0))) {
                  statusBadge = <span className="badge badge-absent" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}><AlertCircle size={12}/> Absent</span>;
                } else {
                  statusBadge = <span className="badge" style={{ background: '#fef3c7', color: '#d97706' }}>Pending</span>;
                }

                return (
                  <tr key={day.dateStr} style={{ borderBottom: '1px solid rgba(0,0,0,0.05)', background: 'transparent' }}>
                    <td style={{ padding: '1rem', fontWeight: 500 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <CalendarIcon size={16} color="var(--text-muted)"/>
                        {formatDateLabel(day.date)}
                      </div>
                    </td>
                    <td style={{ padding: '1rem' }}>{statusBadge}</td>
                    <td style={{ padding: '1rem', color: record ? 'var(--text-main)' : 'var(--text-muted)' }}>{formatTime(record?.checkInTime)}</td>
                    <td style={{ padding: '1rem', color: record ? 'var(--text-main)' : 'var(--text-muted)' }}>{formatTime(record?.checkOutTime)}</td>
                    <td style={{ padding: '1rem' }}>{work > 0 ? `${work.toFixed(1)}h` : '-'}</td>
                    <td style={{ padding: '1rem' }}>{extra > 0 ? <span style={{ color: 'var(--accent-primary)', fontWeight: 500 }}>{extra.toFixed(1)}h</span> : '-'}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  // --- TIME OFF ADMIN VIEW ---
  const renderTimeOffAdminView = () => {
    const leaves = getAllLeaves();
    
    // Filter logic
    const filteredLeaves = leaves.filter(l => {
      if (!searchQuery) return true;
      const q = searchQuery.toLowerCase();
      const n = (l.userName || '').toLowerCase();
      return n.includes(q);
    });

    // Generate Calendar Days
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const blanks = Array(firstDay).fill(null);
    const days = Array.from({ length: daysInMonth }, (_, i) => i + 1);

    return (
      <div className="tab-pane">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
          <h2 className="section-title" style={{ margin: 0 }}>Leave Calendar & Requests</h2>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', background: 'var(--bg-card)', padding: '0.5rem 1rem', borderRadius: '8px', border: '1px solid rgba(0,0,0,0.05)' }}>
            <button onClick={() => { const d = new Date(currentDate); d.setMonth(d.getMonth() - 1); setCurrentDate(d); }} className="btn-icon" style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--text-muted)' }}><ChevronLeft size={20} /></button>
            <span style={{ fontWeight: 600, minWidth: '150px', textAlign: 'center' }}>
              {formatMonthLabel(currentDate)}
            </span>
            <button onClick={() => { const d = new Date(currentDate); d.setMonth(d.getMonth() + 1); setCurrentDate(d); }} className="btn-icon" style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--text-muted)' }}><ChevronRight size={20} /></button>
          </div>

          <div className="search-box" style={{ maxWidth: '250px' }}>
            <Search size={18} className="search-icon" />
            <input
              type="text"
              placeholder="Search employee..."
              className="search-input"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        {/* Calendar Grid */}
        <div style={{ marginBottom: '2rem', display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '0.5rem', background: 'var(--bg-card)', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)' }}>
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => (
            <div key={d} style={{ textAlign: 'center', fontWeight: 'bold', padding: '0.5rem', color: 'var(--text-muted)' }}>{d}</div>
          ))}
          {blanks.map((_, i) => <div key={`b-${i}`} style={{ padding: '0.5rem', minHeight: '80px', background: 'rgba(0,0,0,0.02)', borderRadius: '8px' }}></div>)}
          {days.map(day => {
            const date = new Date(year, month, day);
            const dateTime = date.getTime();
            
            const dayLeaves = filteredLeaves.filter(l => {
               // Only show approved or pending in calendar
               if (l.status === 'Rejected') return false; 
               const from = new Date(l.fromDate).setHours(0,0,0,0);
               const to = new Date(l.toDate).setHours(23,59,59,999);
               return dateTime >= from && dateTime <= to;
            });

            return (
              <div key={day} style={{ padding: '0.5rem', minHeight: '90px', background: 'var(--bg-main)', borderRadius: '8px', border: '1px solid rgba(0,0,0,0.05)' }}>
                <div style={{ fontWeight: '600', marginBottom: '0.5rem', color: 'var(--text-main)' }}>{day}</div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                  {dayLeaves.map(l => (
                    <div key={l.id} style={{ fontSize: '0.7rem', padding: '0.2rem 0.4rem', background: l.status === 'Approved' ? 'var(--accent-primary)' : '#f59e0b', color: 'white', borderRadius: '4px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }} title={`${l.userName} (${l.status})`}>
                      {l.userName}
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>

        <h3 className="pane-title" style={{ marginBottom: '1rem' }}>Leave Requests</h3>

        <div className="table-responsive" style={{ background: 'var(--bg-card)', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead style={{ background: 'rgba(0,0,0,0.02)' }}>
              <tr>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Employee</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Leave Type</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Duration</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Days</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Status</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredLeaves.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>No leave requests found.</td>
                </tr>
              ) : filteredLeaves.map(leave => (
                <tr key={leave.id} style={{ borderBottom: '1px solid rgba(0,0,0,0.05)' }}>
                  <td style={{ padding: '1rem', fontWeight: 500 }}>{leave.userName}</td>
                  <td style={{ padding: '1rem' }}>{leave.type}</td>
                  <td style={{ padding: '1rem' }}>
                    <div style={{ fontSize: '0.85rem' }}>{new Date(leave.fromDate).toLocaleDateString()} - {new Date(leave.toDate).toLocaleDateString()}</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>{leave.reason}</div>
                  </td>
                  <td style={{ padding: '1rem', fontWeight: 500 }}>{leave.days}</td>
                  <td style={{ padding: '1rem' }}>
                    {leave.status === 'Approved' && <span className="badge" style={{ background: '#dcfce7', color: '#166534' }}>Approved</span>}
                    {leave.status === 'Rejected' && <span className="badge" style={{ background: '#fee2e2', color: '#991b1b' }}>Rejected</span>}
                    {leave.status === 'Pending' && <span className="badge" style={{ background: '#fef3c7', color: '#d97706' }}>Pending</span>}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    {leave.status === 'Pending' && (
                      <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <button onClick={() => { updateLeaveStatus(leave.id, 'Approved'); setRefreshTrigger(prev => prev + 1); }} style={{ padding: '0.4rem', border: 'none', background: '#dcfce7', color: '#166534', borderRadius: '4px', cursor: 'pointer' }} title="Approve"><Check size={16} /></button>
                        <button onClick={() => { updateLeaveStatus(leave.id, 'Rejected'); setRefreshTrigger(prev => prev + 1); }} style={{ padding: '0.4rem', border: 'none', background: '#fee2e2', color: '#991b1b', borderRadius: '4px', cursor: 'pointer' }} title="Reject"><X size={16} /></button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  // --- TIME OFF EMPLOYEE VIEW ---
  const renderTimeOffEmployeeView = () => {
    const myLeaves = getLeavesForUser(user?.id);
    
    // Calculate balances
    const PTO_TOTAL = 14;
    const SICK_TOTAL = 7;
    const UNPAID_TOTAL = 0; // usually no limit
    
    let ptoUsed = 0;
    let sickUsed = 0;
    let unpaidUsed = 0;

    myLeaves.forEach(l => {
      if (l.status === 'Approved') {
        if (l.type === 'Paid Time Off') ptoUsed += l.days;
        if (l.type === 'Sick Leave') sickUsed += l.days;
        if (l.type === 'Unpaid Leave') unpaidUsed += l.days;
      }
    });

    return (
      <div className="tab-pane">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <h2 className="section-title" style={{ margin: 0 }}>My Time Off</h2>
          <button className="btn btn-primary" onClick={() => setIsLeaveModalOpen(true)}>
            <Plus size={18} /> New Request
          </button>
        </div>

        {/* Balance Cards */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
          <div style={{ background: 'var(--bg-card)', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', gap: '0.5rem', borderLeft: '4px solid var(--accent-primary)' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: 500 }}>Paid Time Off (PTO)</span>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--text-main)' }}>{Math.max(0, PTO_TOTAL - ptoUsed)} <span style={{ fontSize: '1rem', fontWeight: 400, color: 'var(--text-muted)' }}>days left</span></div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{ptoUsed} used of {PTO_TOTAL} total</div>
          </div>
          <div style={{ background: 'var(--bg-card)', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', gap: '0.5rem', borderLeft: '4px solid #f59e0b' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: 500 }}>Sick Leave</span>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--text-main)' }}>{Math.max(0, SICK_TOTAL - sickUsed)} <span style={{ fontSize: '1rem', fontWeight: 400, color: 'var(--text-muted)' }}>days left</span></div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{sickUsed} used of {SICK_TOTAL} total</div>
          </div>
          <div style={{ background: 'var(--bg-card)', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', gap: '0.5rem', borderLeft: '4px solid #6b7280' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: 500 }}>Unpaid Leave</span>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--text-main)' }}>{unpaidUsed} <span style={{ fontSize: '1rem', fontWeight: 400, color: 'var(--text-muted)' }}>days</span></div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Used this year</div>
          </div>
        </div>

        <h3 className="pane-title" style={{ marginTop: '2rem', marginBottom: '1rem' }}>Request History</h3>
        
        <div className="table-responsive" style={{ background: 'var(--bg-card)', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead style={{ background: 'rgba(0,0,0,0.02)' }}>
              <tr>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Type</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Duration</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Days</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Reason</th>
                <th style={{ padding: '1rem', borderBottom: '1px solid rgba(0,0,0,0.05)', fontWeight: 600 }}>Status</th>
              </tr>
            </thead>
            <tbody>
              {myLeaves.length === 0 ? (
                <tr>
                  <td colSpan="5" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>No time off requests found.</td>
                </tr>
              ) : myLeaves.map(leave => (
                <tr key={leave.id} style={{ borderBottom: '1px solid rgba(0,0,0,0.05)' }}>
                  <td style={{ padding: '1rem', fontWeight: 500 }}>{leave.type}</td>
                  <td style={{ padding: '1rem' }}>{new Date(leave.fromDate).toLocaleDateString()} - {new Date(leave.toDate).toLocaleDateString()}</td>
                  <td style={{ padding: '1rem' }}>{leave.days}</td>
                  <td style={{ padding: '1rem', color: 'var(--text-muted)' }}>{leave.reason || '-'}</td>
                  <td style={{ padding: '1rem' }}>
                    {leave.status === 'Approved' && <span className="badge" style={{ background: '#dcfce7', color: '#166534' }}>Approved</span>}
                    {leave.status === 'Rejected' && <span className="badge" style={{ background: '#fee2e2', color: '#991b1b' }}>Rejected</span>}
                    {leave.status === 'Pending' && <span className="badge" style={{ background: '#fef3c7', color: '#d97706' }}>Pending</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  const handleLeaveSubmit = (e) => {
    e.preventDefault();
    if (!leaveForm.fromDate || !leaveForm.toDate) return;
    const from = new Date(leaveForm.fromDate);
    const to = new Date(leaveForm.toDate);
    const diffTime = Math.abs(to - from);
    const days = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1; // inclusive
    
    requestLeave({
      type: leaveForm.type,
      fromDate: leaveForm.fromDate,
      toDate: leaveForm.toDate,
      reason: leaveForm.reason,
      days: days
    });
    
    setLeaveForm({ type: 'Paid Time Off', fromDate: '', toDate: '', reason: '' });
    setIsLeaveModalOpen(false);
  };

  return (
    <div className="dashboard-container">
      {/* ── Header ── */}
      <header className="dashboard-header">
        <div className="dashboard-brand" style={{ cursor: 'pointer' }} onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={20} style={{ marginRight: '1rem', color: 'var(--text-muted)' }} />
          <h1 className="dashboard-title">Back to Dashboard</h1>
        </div>

        <nav className="dashboard-nav">
          <button className="nav-link" onClick={() => navigate('/dashboard')}>Employees</button>
          <button className={`nav-link ${mainTab === 'Attendance Log' ? 'active' : ''}`} onClick={() => setMainTab('Attendance Log')}>Attendance</button>
          <button className={`nav-link ${mainTab === 'Time Off' ? 'active' : ''}`} onClick={() => setMainTab('Time Off')}>Time Off</button>
        </nav>

        <div className="dashboard-actions">
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
                   My Profile
                </button>
                <button className="dropdown-item" onClick={() => navigate(`/profile?tab=Salary Info`)}>
                   Salary Slip
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
      <main className="dashboard-main" style={{ maxWidth: '1200px', margin: '0 auto', padding: '2rem' }}>
        {/* Inner Tabs for better visual separation */}
        <div className="profile-tabs-header" style={{ marginBottom: '2rem' }}>
          <button className={`profile-tab ${mainTab === 'Attendance Log' ? 'active' : ''}`} onClick={() => setMainTab('Attendance Log')}>Attendance Log</button>
          <button className={`profile-tab ${mainTab === 'Time Off' ? 'active' : ''}`} onClick={() => setMainTab('Time Off')}>Time Off</button>
        </div>

        {mainTab === 'Attendance Log' && (user?.role === 'Admin' || user?.role === 'HR Officer' ? renderAdminView() : renderEmployeeView())}
        {mainTab === 'Time Off' && (user?.role === 'Admin' || user?.role === 'HR Officer' ? renderTimeOffAdminView() : renderTimeOffEmployeeView())}
      </main>

      {/* Leave Request Modal */}
      {isLeaveModalOpen && (
        <Modal isOpen={isLeaveModalOpen} onClose={() => setIsLeaveModalOpen(false)} title="New Time Off Request">
          <form onSubmit={handleLeaveSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div className="form-group">
              <label className="form-label">Employee Name</label>
              <input type="text" className="form-input" value={user?.name || ''} disabled style={{ background: 'var(--bg-main)' }} />
            </div>
            
            <div className="form-group">
              <label className="form-label">Time Off Type</label>
              <select className="form-input" value={leaveForm.type} onChange={(e) => setLeaveForm({...leaveForm, type: e.target.value})} required>
                <option value="Paid Time Off">Paid Time Off (PTO)</option>
                <option value="Sick Leave">Sick Leave</option>
                <option value="Unpaid Leave">Unpaid Leave</option>
              </select>
            </div>

            <div style={{ display: 'flex', gap: '1rem' }}>
              <div className="form-group" style={{ flex: 1 }}>
                <label className="form-label">From Date</label>
                <input type="date" className="form-input" value={leaveForm.fromDate} onChange={(e) => setLeaveForm({...leaveForm, fromDate: e.target.value})} required />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label className="form-label">To Date</label>
                <input type="date" className="form-input" value={leaveForm.toDate} onChange={(e) => setLeaveForm({...leaveForm, toDate: e.target.value})} min={leaveForm.fromDate} required />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Reason</label>
              <textarea className="form-input" rows="3" value={leaveForm.reason} onChange={(e) => setLeaveForm({...leaveForm, reason: e.target.value})} placeholder="Please provide details..."></textarea>
            </div>

            <div className="form-group">
              <label className="form-label">Attachment (Optional)</label>
              <input type="file" className="form-input" style={{ padding: '0.5rem' }} />
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Required for Sick Leave &gt; 2 days</span>
            </div>

            <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
              <button type="button" className="btn btn-outline" onClick={() => setIsLeaveModalOpen(false)} style={{ flex: 1 }}>Cancel</button>
              <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Submit Request</button>
            </div>
          </form>
        </Modal>
      )}

      {/* Edit Attendance Modal (Admin) */}
      {isEditAttOpen && attForm && (
        <Modal isOpen={isEditAttOpen} onClose={() => setIsEditAttOpen(false)} title={`Edit Attendance: ${attForm.empName}`}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', margin: 0 }}>Date: {attForm.dateStr}</p>
            <div className="form-group">
              <label className="form-label" style={{ fontWeight: 500, marginBottom: '0.25rem', display: 'block' }}>Check In Time (HH:MM)</label>
              <input
                type="time"
                style={{ width: '100%', padding: '0.5rem', borderRadius: '4px', border: '1px solid #ccc' }}
                value={attForm.checkInTime}
                onChange={(e) => setAttForm({...attForm, checkInTime: e.target.value})}
              />
            </div>
            <div className="form-group">
              <label className="form-label" style={{ fontWeight: 500, marginBottom: '0.25rem', display: 'block' }}>Check Out Time (HH:MM)</label>
              <input
                type="time"
                style={{ width: '100%', padding: '0.5rem', borderRadius: '4px', border: '1px solid #ccc' }}
                value={attForm.checkOutTime}
                onChange={(e) => setAttForm({...attForm, checkOutTime: e.target.value})}
              />
            </div>
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
              <button className="btn btn-outline" onClick={() => setIsEditAttOpen(false)} style={{ flex: 1 }}>Cancel</button>
              <button 
                className="btn btn-primary" 
                onClick={() => {
                  const att = getAllAttendance();
                  if (!att[attForm.empId]) att[attForm.empId] = {};
                  
                  if (!attForm.checkInTime && !attForm.checkOutTime) {
                    // Mark absent (delete record)
                    delete att[attForm.empId][attForm.dateStr];
                  } else {
                    const dStr = attForm.dateStr;
                    const cIn = attForm.checkInTime ? new Date(`${dStr}T${attForm.checkInTime}:00`).toISOString() : null;
                    const cOut = attForm.checkOutTime ? new Date(`${dStr}T${attForm.checkOutTime}:00`).toISOString() : null;
                    
                    att[attForm.empId][attForm.dateStr] = {
                      date: dStr,
                      checkInTime: cIn,
                      checkOutTime: cOut
                    };
                  }
                  
                  localStorage.setItem('mock_db_attendance', JSON.stringify(att));
                  setAttendanceData(att);
                  setIsEditAttOpen(false);
                }} 
                style={{ flex: 1 }}
              >
                Save Changes
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
