export const mockEmployees = [
  { 
    id: 1, name: 'Priya Sharma', empId: 'EMP-001', email: 'priya.s@hris.com', status: 'present', role: 'HR Manager', department: 'Human Resources', manager: 'Meera Joshi', location: 'Mumbai HQ', joiningDate: 'Jan 10, 2022', phone: '+91 98765 43210', salary: '₹12,00,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-001',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 2, name: 'Rahul Desai', empId: 'EMP-002', email: 'rahul.d@hris.com', status: 'leave', role: 'Software Engineer', department: 'Engineering', manager: 'Sanjay Mehta', location: 'Bengaluru', joiningDate: 'Mar 15, 2023', phone: '+91 98765 12345', salary: '₹15,00,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-002',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 3, name: 'Ananya Singh', empId: 'EMP-003', email: 'ananya.s@hris.com', status: 'absent', role: 'Marketing Specialist', department: 'Marketing', manager: 'Priya Sharma', location: 'Remote', joiningDate: 'Jun 01, 2023', phone: '+91 91234 56789', salary: '₹8,00,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-003',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 4, name: 'Vikram Patel', empId: 'EMP-004', email: 'vikram.p@hris.com', status: 'present', role: 'Product Manager', department: 'Product', manager: 'Meera Joshi', location: 'Mumbai HQ', joiningDate: 'Feb 20, 2021', phone: '+91 99887 76655', salary: '₹18,00,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-004',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 5, name: 'Rohan Gupta', empId: 'EMP-005', email: 'rohan.g@hris.com', status: 'present', role: 'UX Designer', department: 'Design', manager: 'Vikram Patel', location: 'Bengaluru', joiningDate: 'Aug 10, 2023', phone: '+91 98765 54321', salary: '₹14,00,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-005',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 6, name: 'Neha Verma', empId: 'EMP-006', email: 'neha.v@hris.com', status: 'leave', role: 'Sales Executive', department: 'Sales', manager: 'Priya Sharma', location: 'Delhi', joiningDate: 'Nov 05, 2022', phone: '+91 91111 22222', salary: '₹9,00,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-006',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 7, name: 'Sneha Iyer', empId: 'EMP-007', email: 'sneha.i@hris.com', status: 'present', role: 'Frontend Developer', department: 'Engineering', manager: 'Rahul Desai', location: 'Remote', joiningDate: 'May 12, 2024', phone: '+91 93333 44444', salary: '₹11,00,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-007',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 8, name: 'Amit Kumar', empId: 'EMP-008', email: 'amit.k@hris.com', status: 'absent', role: 'Data Analyst', department: 'Data', manager: 'Meera Joshi', location: 'Mumbai HQ', joiningDate: 'Sep 30, 2021', phone: '+91 95555 66666', salary: '₹10,50,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-008',
    resumeUrl: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=800&auto=format&fit=crop'
  },
  { 
    id: 9, name: 'Karthik Reddy', empId: 'EMP-009', email: 'karthik.r@hris.com', status: 'present', role: 'DevOps Engineer', department: 'Engineering', manager: 'Sanjay Mehta', location: 'Remote', joiningDate: 'Oct 15, 2022', phone: '+91 97777 88888', salary: '₹16,00,000',
    avatarUrl: 'https://i.pravatar.cc/150?u=EMP-009',
    resumeUrl: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf'
  },
];

export const getEmployeeById = (id) => {
  return mockEmployees.find(emp => emp.id === parseInt(id));
};
