describe('Bug Reporting Flow', () => {
  it('should login successfully', () => {
    // Visit the login page
    cy.visit('/login');

    // Login
    cy.get('input[name="username"]').type('user15');
    cy.get('input[name="password"]').type('0000');
    cy.get('form').submit();

    // Wait for login to complete and redirect
    cy.url().should('include', '/all-bugs');
  });

  it('should create a new bug and add a comment', () => {
    // Visit the login page
    cy.visit('/login');

    // Login
    cy.get('input[name="username"]').type('user15');
    cy.get('input[name="password"]').type('0000');
    cy.get('form').submit();

    // Wait for login to complete and redirect
    cy.url().should('include', '/all-bugs');

    // Click the create bug button
    cy.contains('Report Bug').click();

    // Fill in the bug report form
    cy.get('input[name="title"]').type('Test Bug Report');
    cy.get('textarea[name="description"]').type('This is a test bug description');
    
    // Select a tag using Material-UI Select
    cy.get('div.MuiFormControl-root').contains('Tags').parent().click();
    cy.get('div.MuiPaper-root li').first().click();
    cy.get('body').click(); // Close the dropdown

    // Submit the form using the bottom button
    cy.get('form').submit();

    // Wait for the bug to be created and verify it's visible
    cy.contains('Test Bug Report').should('be.visible');

    // Click on the bug to view its details
    cy.contains('Test Bug Report').click();

    // Wait for the bug details page to load
    cy.url().should('include', '/bugs/');

    // Add a comment
    cy.get('[data-testid="comment-input"]').type('This is a test comment on the bug');
    cy.get('[data-testid="submit-comment"]').click();

    // Verify the comment was added
    cy.contains('This is a test comment on the bug').should('be.visible');
  });

  it('should delete a bug from user profile', () => {
    // Visit the login page
    cy.visit('/login');

    // Login
    cy.get('input[name="username"]').type('user15');
    cy.get('input[name="password"]').type('0000');
    cy.get('form').submit();

    // Wait for login to complete and redirect
    cy.url().should('include', '/all-bugs');

    // Click the user icon to open the profile menu
    // Target the IconButton containing the AccountCircle icon
    cy.get('button:has(svg[data-testid="AccountCircleIcon"])').click();

    // Add a small wait for the menu to appear
    cy.wait(500);

    // Click 'Profile' in the dropdown menu
    // Wait for the Profile menu item to be visible and then click it
    cy.contains('Profile').should('be.visible').click();

    // Wait for profile page to load
    cy.url().should('include', '/profile');

    // Find and delete the bug we created
    cy.contains('Test Bug Report').parent().parent().within(() => {
      // Target the IconButton containing the DeleteIcon
      cy.get('button:has(svg[data-testid="DeleteIcon"])').click();
    });

    // Confirm deletion in the confirmation dialog
    cy.get('button').contains('Delete').click();

    // Verify the bug is no longer visible
    cy.contains('Test Bug Report').should('not.exist');
  });
}); 