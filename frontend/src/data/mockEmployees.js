export const mockEmployees = [
  { 
    id: 1, name: 'Sarah Jenkins', empId: 'EMP-001', email: 'sarah.j@corehr.com', status: 'present', role: 'HR Manager', department: 'Human Resources', manager: 'Alice Walker', location: 'New York HQ', joiningDate: 'Jan 10, 2022', phone: '+1 234-567-8900', salary: '$95,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-001',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 2, name: 'Michael Chen', empId: 'EMP-002', email: 'michael.c@corehr.com', status: 'leave', role: 'Software Engineer', department: 'Engineering', manager: 'David Smith', location: 'San Francisco', joiningDate: 'Mar 15, 2023', phone: '+1 987-654-3210', salary: '$120,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-002',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 3, name: 'Emily Davis', empId: 'EMP-003', email: 'emily.d@corehr.com', status: 'absent', role: 'Marketing Specialist', department: 'Marketing', manager: 'Sarah Jenkins', location: 'Remote', joiningDate: 'Jun 01, 2023', phone: '+1 456-789-0123', salary: '$75,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-003',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 4, name: 'Robert Fox', empId: 'EMP-004', email: 'robert.f@corehr.com', status: 'present', role: 'Product Manager', department: 'Product', manager: 'Alice Walker', location: 'New York HQ', joiningDate: 'Feb 20, 2021', phone: '+1 321-654-0987', salary: '$110,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-004',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 5, name: 'Cody Fisher', empId: 'EMP-005', email: 'cody.f@corehr.com', status: 'present', role: 'UX Designer', department: 'Design', manager: 'Robert Fox', location: 'San Francisco', joiningDate: 'Aug 10, 2023', phone: '+1 555-123-4567', salary: '$90,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-005',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 6, name: 'Esther Howard', empId: 'EMP-006', email: 'esther.h@corehr.com', status: 'leave', role: 'Sales Executive', department: 'Sales', manager: 'Sarah Jenkins', location: 'Chicago', joiningDate: 'Nov 05, 2022', phone: '+1 777-888-9999', salary: '$85,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-006',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 7, name: 'Jenny Wilson', empId: 'EMP-007', email: 'jenny.w@corehr.com', status: 'present', role: 'Frontend Developer', department: 'Engineering', manager: 'Michael Chen', location: 'Remote', joiningDate: 'May 12, 2024', phone: '+1 111-222-3333', salary: '$105,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-007',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 8, name: 'Guy Hawkins', empId: 'EMP-008', email: 'guy.h@corehr.com', status: 'absent', role: 'Data Analyst', department: 'Data', manager: 'Alice Walker', location: 'New York HQ', joiningDate: 'Sep 30, 2021', phone: '+1 444-555-6666', salary: '$98,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-008',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 9, name: 'Jacob Jones', empId: 'EMP-009', email: 'jacob.j@corehr.com', status: 'present', role: 'DevOps Engineer', department: 'Engineering', manager: 'David Smith', location: 'Remote', joiningDate: 'Oct 15, 2022', phone: '+1 222-333-4444', salary: '$115,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-009',
    resumeUrl: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf'
  },
];

export const getEmployeeById = (id) => {
  return mockEmployees.find(emp => emp.id === parseInt(id));
};
