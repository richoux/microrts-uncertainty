#include <unistd.h>
#include <ios>
#include <iostream>
#include <fstream>
#include <string>
#include <memory>

#include <vector>
#include <numeric>
//#include <chrono>

#include "ghost/variable.hpp"
#include "ghost/solver.hpp"

#include "constraints_rts.hpp"
#include "obj_rts.hpp"

using namespace std;

int main(int argv, char *argc[])
{
  //    std::chrono::steady_clock::time_point start = std::chrono::steady_clock::now();
    
    
    std::ifstream infile(argc[1]);

    int ressources, ph, pr, pl, a, b, c, d;

    int nb_samples = std::stoi(argc[2]);

    std::vector<std::vector<int>> samples;

    for(int i = 0; i < nb_samples; ++i) 
    {
        infile >> a >> b >> c >> d;
        // std::cout << a <<" "<< b <<" "<< c <<" "<< d << std::endl;
        std::vector<int> tmp {a, b, c, d};
        samples.push_back(tmp);
    }
    infile >> ph;
    infile >> pr;
    infile >> pl;
    infile >> ressources;

    // cout << ph << pr << pl << endl;
    // cout << ressources << endl;


    vector<Variable> variables;

    
    
    variables.push_back(Variable("Light_heavy", "Lh", 0, 10+pl));
    variables.push_back(Variable("Heavy_heavy", "Hh", 0, 10+ph));
    variables.push_back(Variable("Ranged_heavy", "Rh", 0, 10+pr));

    variables.push_back(Variable("Light_light", "Ll", 0, 10+pl));
    variables.push_back(Variable("Heavy_light", "Hl", 0, 10+ph));
    variables.push_back(Variable("Ranged_light", "Rl", 0, 10+pr));

    variables.push_back(Variable("Light_ranged", "Lr", 0, 10+pl));
    variables.push_back(Variable("Heavy_ranged", "Hr", 0, 10+ph));
    variables.push_back(Variable("Ranged_ranged", "Rr", 0, 10+pr));

    // variables.push_back(Variable("Light_worker", "Lr", 0, 10+pl));
    // variables.push_back(Variable("Heavy_worker", "Hl", 0, 10+ph));
    // variables.push_back(Variable("Ranged_worker", "Hl", 0, 10+pr));

    variables.push_back(Variable("Plan_light", "pl", pl, 10+pl)); // 9
    variables.push_back(Variable("Plan_heavy", "ph", ph, 10+ph)); // 10
    variables.push_back(Variable("Plan_ranged", "pr", pr, 10+pr)); // 11

    vector<reference_wrapper<Variable>> variables_ref(variables.begin(), variables.end());

    // shared_ptr<Constraint>for_heavy = make_shared<Leq>(variables_ref, vector<double>{0., 0., -1., 1., 0., 0., 0., 0., 0.}, 0);
    shared_ptr<Constraint>ranged = make_shared<Leq>(variables_ref, vector<double>{0., 0., 1., 0., 0., 1., 0., 0., 1., 0., 0., -1.}, 0);
    shared_ptr<Constraint>heavy = make_shared<Leq>(variables_ref, vector<double>{0., 1., 0., 0., 1., 0., 0., 1., 0., 0., -1., 0.}, 0);
    shared_ptr<Constraint>light = make_shared<Leq>(variables_ref, vector<double>{1., 0., 0., 1., 0., 0., 1., 0., 0., -1., 0., 0.}, 0);
    shared_ptr<Constraint>cst_ressources = make_shared<Leq_param>(variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 0., 0., 0., 2., 3., 2.}, ressources, pl, ph, pr);

    // shared_ptr<Constraint>ph_cst = make_shared<Geq>(variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 1., 0., 0.}, ph);
    // shared_ptr<Constraint>pr_cst = make_shared<Geq>(variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 0., 1., 0.}, pr);
    // shared_ptr<Constraint>pl_cst = make_shared<Geq>(variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 0., 0., 1.}, pl);

    vector< shared_ptr<Constraint> > constraints = {ranged, heavy, light, cst_ressources};

    // shared_ptr<Objective> obj = make_shared<MaxDiff>(vector<double>{5., 5., 0.7, 0.5, 1.5, 1.5}, samples);
    shared_ptr<Objective> obj = make_shared<MaxDiff>(vector<double>{0.374, 1., 1.564, 1., 2.675, 0.472, 2.119, 0.639, 1.}, samples);

    Solver solver_p( variables, constraints ,obj );
    
    vector<int>solution(variables.size(), 0);
    double cost_p = 0.;
    // cout << "Solve ..." << endl;
    cout << solver_p.solve(cost_p, solution, 10000, 100000)<< " : "<< cost_p <<" / " <<obj->cost(variables) <<endl;
    cout << solution[10] << solution[11]  << solution[9]<< endl;
    // cout << variables[10].get_value()  << variables[11].get_value()  << variables[9].get_value() << endl;
    cout << solution[10]-ph << endl << solution[11]-pr << endl << solution[9]-pl<<endl;



    // ofstream test_file;
    // test_file.open ("test_file.txt", ios::app);
    // // test_file << solution[9]-ph << " " << solution[10]-pr << " "  << solution[11]-pl<<endl;
    // std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
    // test_file <<  std::chrono::duration_cast<std::chrono::microseconds>(end - start).count()  << " " << solution[9]-pl << " " << solution[10]-ph << " " << solution[11]-pr << endl;
    // test_file.close();

    // cout << variables[8] << endl;
    // cout << pl<<endl;
    // cout << pl_cst->cost() << endl;

    // }else{
        // cout << " no solution"<<endl;
    // }

    // cout << "cost : "<< cost_p << endl;
    // for( int i = 0; i < solution.size(); ++i){
    //     cout << solution[i] <<endl; //<< "  "<< variables[i].get_value() <<endl;
    // }
    // cout << cst_ressources->cost() << endl;
    // for( int i = 0; i < variables.size(); ++i){
    //     cout << variables[i] << endl;
    // }

    // cout << solution[2]<< ">=" << solution[3]<<endl;
    // cout << solution[0] << " + " << solution[4] << " <= " << solution[8] <<endl;
    // cout << solution[1] << " + " << solution[3] << " <= 1.5 * " << solution[7] <<endl;
    // cout << solution[2] << " + " << solution[5] << " <= " << solution[6] <<endl;
    // cout << "3 *"<< solution[6]-ph << " + 2 * " << solution[7]-pr << " + 2 * " <<solution[8]-pl <<" <= " << ressources <<endl;


    // cout << "\n";
    // for(int i = 0 ; i < variables.size(); ++i)
    // {
    //     cout << variables[i].get_value() << endl;
    //     // variables[i].set_value(solution[i]);
    // }

    // cout << "\n";
    // cout << light->cost() << endl;
    // cout << heavy->cost() << endl;
    // cout << ranged->cost() << endl;

    // for(int i = 0 ; i < variables_ref.size(); ++i)
    // {
    //     cout << variables_ref[i] << endl;
    //     // variables[i].set_value(solution[i]);
    // }


    // // var_Lw.set_value(solution[0]);
    // // var_Lr.set_value(solution[4]);
    // // var_pl.set_value(solution[8]);
    // // cout << light->cost() << endl;


    // cout << "\n\n";
    return 0;
}
