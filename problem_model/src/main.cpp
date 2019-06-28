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
#include "phi_function.hpp"

using namespace std;

int main(int argv, char *argc[])
{
	// std::chrono::steady_clock::time_point start = std::chrono::steady_clock::now();

	std::ifstream infile(argc[1]);

	int nb_samples = std::stoi(argc[2]);
	int sampled_worker, sampled_heavy, sampled_range, sampled_light;
	int resources, my_heavy_units, my_range_units, my_light_units;

	std::vector<std::vector<int>> samples;

	for(int i = 0; i < nb_samples; ++i)
	{
		infile >> sampled_worker >> sampled_heavy >> sampled_range >> sampled_light;
		// std::cout << sampled_worker <<" "<< sampled_heavy
		// <<" "<< sampled_range <<" "<< sampled_light << std::"\n";
		std::vector<int> tmp { sampled_worker, sampled_heavy, sampled_range, sampled_light };
		samples.push_back( tmp );
	}
	
	infile >> my_heavy_units;
	infile >> my_range_units;
	infile >> my_light_units;
	infile >> resources;

	// cout << my_heavy_units << my_range_units << my_light_units << "\n";
	// cout << resources << "\n";

	vector<Variable> variables;

	variables.push_back( Variable( "Light_heavy", "Lh", 0, 10 + my_light_units ) );
	variables.push_back( Variable( "Heavy_heavy", "Hh", 0, 10 + my_heavy_units ) );
	variables.push_back( Variable( "Ranged_heavy", "Rh", 0, 10 + my_range_units ) );

	variables.push_back( Variable( "Light_light", "Ll", 0, 10 + my_light_units ) );
	variables.push_back( Variable( "Heavy_light", "Hl", 0, 10 + my_heavy_units ) );
	variables.push_back( Variable( "Ranged_light", "Rl", 0, 10 + my_range_units ) );

	variables.push_back( Variable( "Light_ranged", "Lr", 0, 10 + my_light_units ) );
	variables.push_back( Variable( "Heavy_ranged", "Hr", 0, 10 + my_heavy_units ) );
	variables.push_back( Variable( "Ranged_ranged", "Rr", 0, 10 + my_range_units ) );

	// variables.push_back( Variable( "Light_worker", "Lr", 0, 10 + my_light_units ) );
	// variables.push_back( Variable( "Heavy_worker", "Hl", 0, 10 + my_heavy_units ) );
	// variables.push_back( Variable( "Ranged_worker", "Hl", 0, 10 + my_range_units ) );

	variables.push_back( Variable("Plan_light", "pl", my_light_units, 10 + my_light_units ) ); // 9
	variables.push_back( Variable("Plan_heavy", "ph", my_heavy_units, 10 + my_heavy_units ) ); // 10
	variables.push_back( Variable("Plan_ranged", "pr", my_range_units, 10 + my_range_units ) ); // 11

	vector<reference_wrapper<Variable>> variables_ref( variables.begin(), variables.end() );

	// shared_ptr<Constraint>for_heavy = make_shared<Leq>( variables_ref, vector<double>{0., 0., -1., 1., 0., 0., 0., 0., 0.}, 0 );
	shared_ptr<Constraint>ranged = make_shared<Leq>( variables_ref, vector<double>{0., 0., 1., 0., 0., 1., 0., 0., 1., 0., 0., -1.}, 0 );
	shared_ptr<Constraint>heavy = make_shared<Leq>( variables_ref, vector<double>{0., 1., 0., 0., 1., 0., 0., 1., 0., 0., -1., 0.}, 0 );
	shared_ptr<Constraint>light = make_shared<Leq>( variables_ref, vector<double>{1., 0., 0., 1., 0., 0., 1., 0., 0., -1., 0., 0.}, 0 );
	shared_ptr<Constraint>cst_resources = make_shared<Leq_param>( variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 0., 0., 0., 2., 3., 2.}, resources, my_light_units, my_heavy_units, my_range_units );

	// shared_ptr<Constraint>my_heavy_units_cst = make_shared<Geq>( variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 1., 0., 0.}, my_heavy_units );
	// shared_ptr<Constraint>my_range_units_cst = make_shared<Geq>( variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 0., 1., 0.}, my_range_units );
	// shared_ptr<Constraint>my_light_units_cst = make_shared<Geq>( variables_ref, vector<double>{0., 0., 0., 0., 0., 0., 0., 0., 1.}, my_light_units );

	vector< shared_ptr<Constraint> > constraints = {ranged, heavy, light, cst_resources};

	auto phi_callback = identity();
	//auto phi_callback = pessimistic();
	//auto phi_callback = optimistic();

	// shared_ptr<Objective> obj = make_shared<MaxDiff>( vector<double>{5., 5., 0.7, 0.5, 1.5, 1.5}, samples );
	shared_ptr<Objective> obj = make_shared<MaxDiff>( vector<double>{0.374, 1., 1.564, 1., 2.675, 0.472, 2.119, 0.639, 1.},
																										samples,
																										phi_callback );

	Solver solver_p( variables, constraints, obj );

	vector<int>solution( variables.size(), 0 );
	double cost_p = 0.;

	// cout << "Solve ..." << "\n";
	cout << solver_p.solve( cost_p, solution, 10000, 100000 ) << " : " << cost_p << " / " << obj->cost( variables ) << "\n";
	cout << solution[10] << solution[11]  << solution[9] << "\n";
	// cout << variables[10].get_value()  << variables[11].get_value()  << variables[9].get_value() << "\n";
	cout << solution[10] - my_heavy_units << "\n" << solution[11] - my_range_units << "\n" << solution[9] - my_light_units <<"\n";

	// ofstream test_file;
	// test_file.open ("test_file.txt", ios::app);
	// // test_file << solution[9]-my_heavy_units << " " << solution[10]-my_range_units << " "  << solution[11]-my_light_units<<"\n";
	// std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
	// test_file <<  std::chrono::duration_cast<std::chrono::microseconds>(end - start).count()  << " " << solution[9]-my_light_units << " " << solution[10]-my_heavy_units << " " << solution[11]-my_range_units << "\n";
	// test_file.close();

	// cout << variables[8] << "\n";
	// cout << my_light_units<<"\n";
	// cout << my_light_units_cst->cost() << "\n";

	// }else{
	// cout << " no solution"<<"\n";
	// }

	// cout << "cost : "<< cost_p << "\n";
	// for( int i = 0; i < solution.size(); ++i){
	//     cout << solution[i] <<"\n"; //<< "  "<< variables[i].get_value() <<"\n";
	// }
	// cout << cst_resources->cost() << "\n";
	// for( int i = 0; i < variables.size(); ++i){
	//     cout << variables[i] << "\n";
	// }

	// cout << solution[2]<< ">=" << solution[3]<<"\n";
	// cout << solution[0] << " + " << solution[4] << " <= " << solution[8] <<"\n";
	// cout << solution[1] << " + " << solution[3] << " <= 1.5 * " << solution[7] <<"\n";
	// cout << solution[2] << " + " << solution[5] << " <= " << solution[6] <<"\n";
	// cout << "3 *"<< solution[6]-my_heavy_units << " + 2 * " << solution[7]-my_range_units << " + 2 * " <<solution[8]-my_light_units <<" <= " << resources <<"\n";


	// cout << "\n";
	// for(int i = 0 ; i < variables.size(); ++i)
	// {
	//     cout << variables[i].get_value() << "\n";
	//     // variables[i].set_value(solution[i]);
	// }

	// cout << "\n";
	// cout << light->cost() << "\n";
	// cout << heavy->cost() << "\n";
	// cout << ranged->cost() << "\n";

	// for(int i = 0 ; i < variables_ref.size(); ++i)
	// {
	//     cout << variables_ref[i] << "\n";
	//     // variables[i].set_value(solution[i]);
	// }


	// // var_Lw.set_value(solution[0]);
	// // var_Lr.set_value(solution[4]);
	// // var_pl.set_value(solution[8]);
	// // cout << light->cost() << "\n";


	// cout << "\n\n";
	return 0;
}
