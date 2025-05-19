const token = localStorage.getItem('token'); // Retrieve the token from localStorage
console.log("Token:", token);

// Fetch user email and display it only if logged in
async function fetchUserEmail() {
    if (!token) return; // Skip fetching user email if not logged in

    const userId = localStorage.getItem('userId');
    if (!userId) {
        console.error("User ID not found.");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8081/api/v1/camps-jobs/fullname?userId=${userId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch user details');
        }

        const userData = await response.json();
        console.log("User Data:", userData);
        document.getElementById('username').textContent = `Welcome, ${userData}`;
    } catch (error) {
        console.error('Error fetching user email:', error);
    }
}

// Fetch employer name using jobId
async function fetchEmployerName(jobId) {
    try {
        const response = await fetch(`http://localhost:8081/api/employer/employer-name?jobId=${jobId}`);
        if (!response.ok) {
            throw new Error('Failed to fetch employer name');
        }
        return await response.text();
    } catch (error) {
        console.error('Error fetching employer name:', error);
        return 'Unknown Employer';
    }
}

// Fetch jobs dynamically
// Fetch jobs dynamically with search functionality
async function fetchJobs(searchCriteria = {}) {
    const jobContainer = document.querySelector(".job-cards");
    try {
        const response = await fetch('http://localhost:8081/api/job/all-jobs');
        if (!response.ok) {
            throw new Error('Failed to fetch jobs');
        }

        const jobs = await response.json();
        jobContainer.innerHTML = "";

        // Filter jobs based on search criteria
        const filteredJobs = await Promise.all(
            jobs.map(async (job) => {
                if (job.jobStatus !== "Approved") return null; // Skip unapproved jobs

                // Fetch employer name for each job
                const employerName = await fetchEmployerName(job.jobId);

                // Check if the job matches the search criteria
                const matchesEmployer = searchCriteria.employer
                    ? employerName.toLowerCase().includes(searchCriteria.employer.toLowerCase())
                    : true;

                const matchesJobTitle = searchCriteria.jobTitle
                    ? job.jobTitle.toLowerCase().includes(searchCriteria.jobTitle.toLowerCase())
                    : true;

                const matchesSalary = searchCriteria.salary
                    ? job.jobSalary && job.jobSalary >= parseInt(searchCriteria.salary)
                    : true;

                // Return the job with employer name if it matches all criteria
                if (matchesEmployer && matchesJobTitle && matchesSalary) {
                    return { ...job, employerName };
                }

                return null; // Exclude jobs that don't match
            })
        );

        // Remove null values from the filtered jobs array
        const validJobs = filteredJobs.filter(job => job !== null);

        if (validJobs.length === 0) {
            jobContainer.innerHTML = "<p>No jobs match your search criteria.</p>";
            return;
        }

        // Display the filtered jobs
        for (const job of validJobs) {
            const jobCard = document.createElement("div");
            jobCard.classList.add("card");
            jobCard.innerHTML = `
                <h3 style="color: maroon;"><i class="fas fa-building"></i> ${job.employerName}</h3>
                <hr style="height: 2px; background-color: purple; border: none;">
                <h3 class="job-title"><i class="fas fa-briefcase"></i> ${job.jobTitle}</h3>
                <p class="job-type"><i class="fas fa-clock"></i> ${job.jobType}</p>
                <p class="location"><i class="fas fa-map-marker-alt"></i> ${job.jobLocation}</p>
                <p class="salary"><i class="fas fa-money-bill-wave"></i> Ksh ${job.jobSalary ? job.jobSalary.toLocaleString() : 'N/A'}</p>
                <p class="expiry-date"><i class="fas fa-calendar-times"></i> Expires on: ${new Date(job.expiryDate).toLocaleDateString()}</p>
                <button class="view-details" data-jobid="${job.jobId}">
                    <i class="fas fa-eye"></i> View details
                </button>
            `;
            jobContainer.appendChild(jobCard);
        }

        // Add event listeners to the "View details" buttons
        document.querySelectorAll('.view-details').forEach(button => {
            button.addEventListener('click', function () {
                const jobId = this.getAttribute('data-jobid');
                viewJobDetails(jobId);
            });
        });
    } catch (error) {
        console.error('Error fetching jobs:', error);
        jobContainer.innerHTML = "<p>Failed to load jobs. Please try again later.</p>";
    }
}

// View job details
async function viewJobDetails(jobId) {
    const jobDetailsSection = document.getElementById('job-details');
    const jobDetailsContent = document.getElementById('job-details-content');
    try {
        const jobResponse = await fetch(`http://localhost:8081/api/job/info/${jobId}`);
        if (!jobResponse.ok) {
            throw new Error('Failed to fetch job details');
        }
        const job = await jobResponse.json();
        const employerName = await fetchEmployerName(jobId);

        jobDetailsContent.innerHTML = 
        `<h2 style="color: maroon;">${employerName}</h2>
        <hr style="height: 3px; background-color: purple; border: none;">
        <h3>${job.jobTitle}</h3>
        <hr style="height: 3px; background-color: purple; border: none;">
        <p><strong>Type:</strong> ${job.jobType}</p>
        <p><strong>Location:</strong> ${job.jobLocation}</p>
        <p><strong>Salary:</strong> Ksh ${job.jobSalary ? job.jobSalary.toLocaleString() : 'N/A'}</p>
        <hr style="height: 3px; background-color: purple; border: none;">
        <p><strong>Description:</strong> ${job.jobDesc}</p>
        <hr style="height: 3px; background-color: purple; border: none;">
        <p><strong>Requirements:</strong> ${job.jobReqs}</p>
        <hr style="height: 3px; background-color: purple; border: none;">
        <p><strong>Expires on:</strong> ${job.expiryDate ? new Date(job.expiryDate).toLocaleDateString() : 'N/A'}</p>
        <hr style="height: 3px; background-color: purple; border: none;">
        <button class="apply-btn" onclick="handleApply()">Apply</button>`;
        
        jobDetailsSection.classList.add('active');
    } catch (error) {
        console.error('Error fetching job details:', error);
        alert('Failed to load job details.');
    }
}

// Handle Apply button click
function handleApply() {
    if (!token) {
        alert("LOGIN TO APPLY.");
    } else {
        alert("LOGIN TO APPLY.");
    }
}

// Close job details
function closeJobDetails() {
    document.getElementById('job-details').classList.remove('active');
}

// Initialize the page
document.addEventListener('DOMContentLoaded', () => {
    fetchUserEmail();
    fetchJobs();

    // Add event listeners for search inputs
    document.getElementById('employerSearch').addEventListener('input', handleSearch);
    document.getElementById('jobTitleSearch').addEventListener('input', handleSearch);
    document.getElementById('salarySearch').addEventListener('input', handleSearch);
});

// Handle search input changes
function handleSearch() {
    const employer = document.getElementById('employerSearch').value;
    const jobTitle = document.getElementById('jobTitleSearch').value;
    const salary = document.getElementById('salarySearch').value;

    fetchJobs({ employer, jobTitle, salary });
}